#include "Arguments.hpp"
#include "Client.hpp"
#include "Shell.hpp"

int main(const int argc, const char *argv[])
{
  // Verify that the version of the library that we linked against is
  // compatible with the version of the headers we compiled against.
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  Client client;
  client.connect();

  if (1 < argc) {
    Arguments arguments(argc, argv);
    arguments.runCommandLoop(client);
  }
  else {
    Shell shell;
    shell.runCommandLoop(client);
  }

  client.disconnect();

  // Optional: Delete all global objects allocated by libprotobuf.
  google::protobuf::ShutdownProtobufLibrary();

  return 0;
}
