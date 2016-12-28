#include "Client.hpp"

#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/tcp.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <unistd.h>

#include <iostream>
#include <sstream>

#include "Delay.hpp"

namespace {
const bool debugLogging =
    (::getenv("DEBUG_LOGGING") && 0 == ::strcmp(::getenv("DEBUG_LOGGING"), "true"));

const int headerSize = 9;

void dump(const char *caption, const char *buf, const ssize_t length)
{
  if (caption) {
    std::cout << caption << ": ";
  }
  for (int z = 0; z < length; ++z) {
    if (0 < z) {
      std::cout << ' ';
    }
    const int b = (0xFF & (int)buf[z]);
    const int h = ((0xF0 & b) >> 4);
    std::cout << (char)(h < 10 ? '0' + h : '7' + h);
    const int l = (0x0F & b);
    std::cout << (char)(l < 10 ? '0' + l : '7' + l);
  }
  std::cout << std::endl;
}

message::Header_MessageType getMessageType(const ::google::protobuf::Message *message)
{
  if (0 == ::strcmp(typeid(message::ConnectRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_CONNECT_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::ConnectReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_CONNECT_REPLY;
  }
  else if (0 == ::strcmp(typeid(message::DisconnectRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_DISCONNECT_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::DisconnectReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_DISCONNECT_REPLY;
  }
  else if (0 == ::strcmp(typeid(message::PutRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_PUT_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::PutReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_PUT_REPLY;
  }
  else if (0 == ::strcmp(typeid(message::GetRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_GET_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::GetReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_GET_REPLY;
  }
  else if (0 == ::strcmp(typeid(message::InvalidateRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_INVALIDATE_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::InvalidateReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_INVALIDATE_REPLY;
  }
  else if (0 == ::strcmp(typeid(message::DestroyRequest).name(), typeid(*message).name())) {
    return message::Header_MessageType_DESTROY_REQUEST;
  }
  else if (0 == ::strcmp(typeid(message::DestroyReply).name(), typeid(*message).name())) {
    return message::Header_MessageType_DESTROY_REPLY;
  }
  return message::Header_MessageType_UNSPECIFIED_TYPE;
}

::google::protobuf::Message *getMessageBuilder(const message::Header_MessageType messageType)
{
  if (message::Header_MessageType_CONNECT_REQUEST == messageType) {
    return new message::ConnectRequest();
  }
  else if (message::Header_MessageType_CONNECT_REPLY == messageType) {
    return new message::ConnectReply();
  }
  else if (message::Header_MessageType_DISCONNECT_REQUEST == messageType) {
    return new message::DisconnectRequest();
  }
  else if (message::Header_MessageType_DISCONNECT_REPLY == messageType) {
    return new message::DisconnectReply();
  }
  else if (message::Header_MessageType_PUT_REQUEST == messageType) {
    return new message::PutRequest();
  }
  else if (message::Header_MessageType_PUT_REPLY == messageType) {
    return new message::PutReply();
  }
  else if (message::Header_MessageType_GET_REQUEST == messageType) {
    return new message::GetRequest();
  }
  else if (message::Header_MessageType_GET_REPLY == messageType) {
    return new message::GetReply();
  }
  else if (message::Header_MessageType_INVALIDATE_REQUEST == messageType) {
    return new message::InvalidateRequest();
  }
  else if (message::Header_MessageType_INVALIDATE_REPLY == messageType) {
    return new message::InvalidateReply();
  }
  else if (message::Header_MessageType_DESTROY_REQUEST == messageType) {
    return new message::DestroyRequest();
  }
  else if (message::Header_MessageType_DESTROY_REPLY == messageType) {
    return new message::DestroyReply();
  }
  return NULL;
}

int connectSocket()
{
  int sock = ::socket(AF_INET, SOCK_STREAM, 0);
  if (-1 != sock) {
    struct hostent *he = ::gethostbyname("localhost");
    if (he != NULL) {
      struct sockaddr_in server;
      struct in_addr **addr_list = (struct in_addr **)he->h_addr_list;
      for (int i = 0; addr_list[i] != NULL; i++) {
        server.sin_addr = *addr_list[i];
        if (debugLogging) {
          std::cout << "localhost resolved to " << inet_ntoa(*addr_list[i]) << std::endl;
        }
        break;
      }
      server.sin_family = AF_INET;
      server.sin_port = htons(8000);

      if (0 <= ::connect(sock, (struct sockaddr *)&server, sizeof(server))) {
        if (debugLogging) {
          std::cout << "Connected. :)" << std::endl;
        }
        ::fcntl(sock, F_SETFL, O_NONBLOCK);
      }
    }
    else {
      sock = -1;
      std::cerr << "Unable to resolve host name. :(" << std::endl;
    }
  }
  else {
    std::cerr << "Unable to create socket. :(" << std::endl;
  }
  return sock;
}

void disconnectSocket(int sock)
{
  if (-1 != sock) {
    ::close(sock);
  }
}

ssize_t send(int sock, const char *buf, const ssize_t n)
{
  int flag = 0;
  ::setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&flag, sizeof(int));
  const ssize_t count = ::send(sock, buf, n, MSG_DONTWAIT);
  flag = 1;
  ::setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&flag, sizeof(int));
  if (debugLogging) {
    std::cout << "Sent " << count << " bytes." << std::endl;
  }
  return count;
}

ssize_t receive(int sock, char *buf, const ssize_t n)
{
  ssize_t count = 0;
  /*while*/ if (count < n) {
    ::fsync(sock);

    fd_set readset;
    FD_ZERO(&readset);
    FD_SET(sock, &readset);

    struct timeval tv;
    tv.tv_sec = 2;
    tv.tv_usec = 500000;

    const int result = ::select(sock + 1, &readset, NULL, NULL, &tv);
    if (0 < result && FD_ISSET(sock, &readset)) {
      const ssize_t received = ::recv(sock, buf, n, 0);
      if (-1 != received) {
        buf += received;
        count += received;
      }
      else {
        return received;
      }
    }
    else if (0 == result) {
      std::cerr << "Timed out. :(" << std::endl;
    }
    else {
      std::cerr << "Unable to select. :(" << std::endl;
    }
  }
  if (debugLogging) {
    std::cout << "Received " << count << " bytes." << std::endl;
  }
  return count;
}
}  // namespace

Client::Client() : _sock(-1), _id(-1)
{
  // NOP
}

Client::~Client()
{
  disconnectSocket(_sock);
  _sock = -1;
}

bool Client::connected() const
{
  return -1 != _id;
}

void Client::connect()
{
  _sock = connectSocket();
  if (-1 != _sock) {
    if (debugLogging) {
      std::cout << "Got a server. :)" << std::endl;
    }
    try {
      message::ConnectRequest *connectRequest = dynamic_cast<message::ConnectRequest *>(
          getMessageBuilder(message::Header_MessageType_CONNECT_REQUEST));
      connectRequest->set_id(-1);  // Any non-zero number will work...
      sendMessage(connectRequest);
      message::ConnectReply *connectReply = dynamic_cast<message::ConnectReply *>(receiveMessage());
      if (connectReply) {
        _id = connectReply->id();
      }
    }
    catch (...) {
      std::cerr << "Unable to connect to server. :(" << std::endl;
    }
  }
  if (debugLogging) {
    std::cout << "Connected as " << _id << ". :)" << std::endl;
  }
}

void Client::disconnect()
{
  if (connected()) {
    try {
      message::DisconnectRequest *disconnectRequest = dynamic_cast<message::DisconnectRequest *>(
          getMessageBuilder(message::Header_MessageType_DISCONNECT_REQUEST));
      disconnectRequest->set_id(_id);
      sendMessage(disconnectRequest);
      message::DisconnectReply *disconnectReply =
          dynamic_cast<message::DisconnectReply *>(receiveMessage());
      if (disconnectReply) {
        _id = -1;
      }
      if (debugLogging) {
        std::cout << "Released a server. :)" << std::endl;
      }
    }
    catch (...) {
      std::cerr << "Unable to disconnect from server. :(" << std::endl;
    }
  }
  disconnectSocket(_sock);
  _sock = -1;
  _id = -1;
}

void Client::put(const std::string &region, const std::string &key, const std::string &value)
{
  if (connected()) {
    try {
      message::PutRequest *putRequest = dynamic_cast<message::PutRequest *>(
          getMessageBuilder(message::Header_MessageType_PUT_REQUEST));
      putRequest->set_region(region);
      message::Pair *pair = putRequest->add_pair();
      pair->set_key(key);
      pair->set_value(value);
      sendMessage(putRequest);
      message::PutReply *putReply = dynamic_cast<message::PutReply *>(receiveMessage());
      if (putReply) {
        if (debugLogging) {
          std::cout << "Put " << putReply->count() << " values. :)" << std::endl;
        }
      }
    }
    catch (...) {
      std::cerr << "Unable to put values. :(" << std::endl;
    }
  }
}

std::string Client::get(const std::string &region, const std::string &key)
{
  std::string value;

  if (connected()) {
    try {
      message::GetRequest *getRequest = dynamic_cast<message::GetRequest *>(
          getMessageBuilder(message::Header_MessageType_GET_REQUEST));
      getRequest->set_region(region);
      getRequest->add_key(key);
      sendMessage(getRequest);
      message::GetReply *getReply = dynamic_cast<message::GetReply *>(receiveMessage());
      if (getReply) {
        if (debugLogging) {
          std::cout << "Got " << getReply->pair_size() << " values. :)" << std::endl;
        }
        for (int i = 0; i < getReply->pair_size(); ++i) {
          if (key == getReply->pair(i).key()) {
            value = getReply->pair(i).value();
            break;
          }
        }
      }
    }
    catch (...) {
      std::cerr << "Unable to get values. :(" << std::endl;
    }
  }

  return value;
}

void Client::invalidate(const std::string &region, const std::string &key)
{
  if (connected()) {
    try {
      message::InvalidateRequest *invalidateRequest = dynamic_cast<message::InvalidateRequest *>(
          getMessageBuilder(message::Header_MessageType_INVALIDATE_REQUEST));
      invalidateRequest->set_region(region);
      invalidateRequest->add_key(key);
      sendMessage(invalidateRequest);
      message::InvalidateReply *invalidateReply =
          dynamic_cast<message::InvalidateReply *>(receiveMessage());
      if (invalidateReply) {
        if (debugLogging) {
          std::cout << "Invalidated " << invalidateReply->count() << " keys. :)" << std::endl;
        }
      }
    }
    catch (...) {
      std::cerr << "Unable to invalidate keys. :(" << std::endl;
    }
  }
}

void Client::destroy(const std::string &region, const std::string &key)
{
  if (connected()) {
    try {
      message::DestroyRequest *destroyRequest = dynamic_cast<message::DestroyRequest *>(
          getMessageBuilder(message::Header_MessageType_DESTROY_REQUEST));
      destroyRequest->set_region(region);
      destroyRequest->add_key(key);
      sendMessage(destroyRequest);
      message::DestroyReply *destroyReply = dynamic_cast<message::DestroyReply *>(receiveMessage());
      if (destroyReply) {
        if (debugLogging) {
          std::cout << "Destroyd " << destroyReply->count() << " keys. :)" << std::endl;
        }
      }
    }
    catch (...) {
      std::cerr << "Unable to destroy keys. :(" << std::endl;
    }
  }
}

void Client::runCommandLoop(Commands &commands)
{
    Delay delay;
    while (commands) {
        if (delay) {
            if (debugLogging) {
                std::cout << "Sleeping " << delay.seconds() << " seconds..." << std::endl;
            }
            delay();
        }
        
        delay.reset();
        if (*commands == "quit") {
            return;
        }
        else if (*commands == "sleep") {
            delay.parse(*(++commands));
        }
        else if (*commands == "put") {
            const std::string region(*(++commands));
            const std::string key(*(++commands));
            const std::string value(*(++commands));
            if (!region.empty() && !key.empty()) {
                std::cout << std::endl;
                std::cout << key << "=" << value << std::endl;
                
                put(region, key, value);
                delay.randomize();
            }
        }
        else if (*commands == "get") {
            const std::string region(*(++commands));
            const std::string key(*(++commands));
            if (!region.empty() && !key.empty()) {
                const std::string value(get(region, key));
                delay.randomize();
                
                std::cout << std::endl;
                std::cout << key << "=" << value << std::endl;
            }
        }
        else if (*commands == "invalidate") {
            const std::string region(*(++commands));
            const std::string key(*(++commands));
            if (!region.empty() && !key.empty()) {
                invalidate(region, key);
                delay.randomize();
            }
        }
        else if (*commands == "destroy") {
            const std::string region(*(++commands));
            const std::string key(*(++commands));
            if (!region.empty() && !key.empty()) {
                destroy(region, key);
                delay.randomize();
            }
        }
        else {
            if (*commands != "help") {
                std::cerr << "Unknown command: " << *commands << std::endl;
            }
            std::cout << "Valid commands:" << std::endl;
            std::cout << "\thelp                       -- print this help message" << std::endl;
            std::cout << "\tquit                       -- exit" << std::endl;
            std::cout << "\tsleep <duration>           -- wait for the duration" << std::endl;
            std::cout << "\tget <region> <key>         -- get the value for a key" << std::endl;
            std::cout << "\tput <region> <key> <value> -- put the value for a key" << std::endl;
            std::cout << "\tinvalidate <region> <key>  -- invalidate the key" << std::endl;
            std::cout << "\tdestroy <region> <key>     -- destroy the key" << std::endl;
        }
        
        ++commands;
    }
}

void Client::sendMessage(const ::google::protobuf::Message *message)
{
  std::ostringstream messageStream;
  if (message->SerializeToOstream(&messageStream)) {
    const std::string messageBuffer(messageStream.str());
    if (debugLogging) {
      dump("SND[MSG]", messageBuffer.c_str(), messageBuffer.length());
    }
    message::Header header;
    header.set_messagetype(getMessageType(message));
    header.set_messagesize(messageBuffer.length());
    header.set_ok(true);
    if (debugLogging) {
      std::cout << "SND " << header.messagetype() << std::endl;
    }
    std::ostringstream headerStream;
    if (header.SerializeToOstream(&headerStream)) {
      const std::string headerBuffer(headerStream.str());
      if (debugLogging) {
        dump("SND[HDR]", headerBuffer.c_str(), headerBuffer.length());
      }
      if (headerBuffer.length() == send(_sock, headerBuffer.c_str(), headerBuffer.length())) {
        if (messageBuffer.length() == send(_sock, messageBuffer.c_str(), messageBuffer.length())) {
          // NOP
        }
        else {
          std::cerr << "Unable to send message. :(" << std::endl;
        }
      }
      else {
        std::cerr << "Unable to send header. :(" << std::endl;
      }
    }
    else {
      std::cerr << "Unable to serialize header. :(" << std::endl;
    }
  }
  else {
    std::cerr << "Unable to serialize message. :(" << std::endl;
  }
}

::google::protobuf::Message *Client::receiveMessage()
{
  char buf[1024];
  if (headerSize == receive(_sock, buf, headerSize)) {
    if (debugLogging) {
      dump("RCV[HDR]", buf, headerSize);
    }
    message::Header header;
    std::istringstream input(std::string(buf, headerSize));
    if (header.ParseFromIstream(&input)) {
      if (debugLogging) {
        std::cout << "RCV " << header.messagetype() << std::endl;
      }
      if (header.messagesize() == receive(_sock, buf, header.messagesize())) {
        if (debugLogging) {
          dump("RCV[MSG]", buf, header.messagesize());
        }
        ::google::protobuf::Message *message = getMessageBuilder(header.messagetype());
        std::istringstream input(std::string(buf, header.messagesize()));
        if (message->ParseFromIstream(&input)) {
          return message;
        }
        else {
          std::cerr << "Unable to deserialize message. :(" << std::endl;
        }
      }
      else {
        std::cerr << "Unable to receive message. :(" << std::endl;
      }
    }
    else {
      std::cerr << "Unable to deserialize header. :(" << std::endl;
    }
  }
  else {
    std::cerr << "Unable to receive header. :(" << std::endl;
  }
  return NULL;
}

