#pragma once

#ifdef __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
#endif
#include "message.pb.h"
#ifdef __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__
#pragma clang diagnostic pop
#endif

class Client {
 public:
  Client();

  ~Client();

  bool connected() const;

  void connect();

  void disconnect();

  std::string get(const std::string& region, const std::string& key);

  void put(const std::string& region, const std::string& key, const std::string& value);

  void invalidate(const std::string& region, const std::string& key);

  void destroy(const std::string& region, const std::string& key);

 private:
  int _sock;

  int _id;

  void sendMessage(const ::google::protobuf::Message* message);

  ::google::protobuf::Message* receiveMessage();
};
