#include "Arguments.hpp"
#include "Client.hpp"
#include "Delay.hpp"
#include "Shell.hpp"

namespace {
const bool debugLogging =
    (::getenv("DEBUG_LOGGING") && 0 == ::strcmp(::getenv("DEBUG_LOGGING"), "true"));
    
void runCommandLoop(Client &client, Commands &commands)
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

        client.put(region, key, value);
        delay.randomize();
      }
    }
    else if (*commands == "get") {
      const std::string region(*(++commands));
      const std::string key(*(++commands));
      if (!region.empty() && !key.empty()) {
        const std::string value(client.get(region, key));
        delay.randomize();

        std::cout << std::endl;
        std::cout << key << "=" << value << std::endl;
      }
    }
    else if (*commands == "invalidate") {
      const std::string region(*(++commands));
      const std::string key(*(++commands));
      if (!region.empty() && !key.empty()) {
        client.invalidate(region, key);
        delay.randomize();
      }
    }
    else if (*commands == "destroy") {
      const std::string region(*(++commands));
      const std::string key(*(++commands));
      if (!region.empty() && !key.empty()) {
        client.destroy(region, key);
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
}  // namespace

int main(const int argc, const char *argv[])
{
  // Verify that the version of the library that we linked against is
  // compatible with the version of the headers we compiled against.
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  Client client;
  client.connect();

  if (1 < argc) {
    Arguments arguments(argc, argv);
    runCommandLoop(client, arguments);
  }
  else {
    Shell shell;
    runCommandLoop(client, shell);
  }

  client.disconnect();

  // Optional: Delete all global objects allocated by libprotobuf.
  google::protobuf::ShutdownProtobufLibrary();

  return 0;
}
