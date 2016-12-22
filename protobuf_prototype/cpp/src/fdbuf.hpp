#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/tcp.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <streambuf>

#ifdef __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
#endif
#include "message.pb.h"
#ifdef __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__
#pragma clang diagnostic pop
#endif
using message::ConnectRequest;
using message::Header;

class fdbuf : public std::streambuf {
 private:
  enum { bufsize = 1024 };
  char outbuf_[bufsize];
  char inbuf_[bufsize + 16 - sizeof(int)];
  int fd_;

 public:
  typedef std::streambuf::traits_type traits_type;

  fdbuf(int fd);
  ~fdbuf();
  void open(int fd);
  void close();

 protected:
  int overflow(int c);
  int underflow();
  int sync();
};
