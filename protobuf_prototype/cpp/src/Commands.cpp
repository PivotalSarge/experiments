#include "Commands.hpp"

#include <sstream>

#include "Delay.hpp"

namespace {
const bool debugLogging = (::getenv("DEBUG_LOGGING") &&
                           0 == ::strcmp(::getenv("DEBUG_LOGGING"), "true"));
}  // namespace

Commands::Commands() {
  // NOP
}

Commands::~Commands() {
  // NOP
}

const std::string &Commands::operator*() const {
  if (!_commands.empty()) {
    return _commands.front();
  }
  static const std::string none;
  return none;
}

Commands &Commands::operator++() {
  if (!_commands.empty()) {
    _commands.pop_front();
  }
  return *this;
}

void Commands::runCommandLoop(Client &client) {
  Delay delay;
  while (*this) {
    if (delay) {
      if (debugLogging) {
        std::cout << "Sleeping " << delay.seconds() << " seconds..."
                  << std::endl;
      }
      delay();
    }
    delay.reset();

    const std::string &command(*(*this));
    if (command == "quit") {
      return;
    } else if (command == "sleep") {
      delay.parse(*(++(*this)));
    } else if (command == "put") {
      const std::string region(*(++(*this)));
      const std::string key(*(++(*this)));
      const std::string value(*(++(*this)));
      if (!region.empty() && !key.empty()) {
        std::cout << std::endl;
        std::cout << key << "=" << value << std::endl;

        client.put(region, key, value);
        delay.randomize();
      }
    } else if (command == "get") {
      const std::string region(*(++(*this)));
      const std::string key(*(++(*this)));
      if (!region.empty() && !key.empty()) {
        const std::string value(client.get(region, key));
        delay.randomize();

        std::cout << std::endl;
        std::cout << key << "=" << value << std::endl;
      }
    } else if (command == "invalidate") {
      const std::string region(*(++(*this)));
      const std::string key(*(++(*this)));
      if (!region.empty() && !key.empty()) {
        client.invalidate(region, key);
        delay.randomize();
      }
    } else if (command == "destroy") {
      const std::string region(*(++(*this)));
      const std::string key(*(++(*this)));
      if (!region.empty() && !key.empty()) {
        client.destroy(region, key);
        delay.randomize();
      }
    } else {
      if (command != "help") {
        std::cerr << "Unknown command: " << command << std::endl;
      }
      std::cout << "Valid commands:" << std::endl;
      std::cout << "\thelp                       -- print this help message"
                << std::endl;
      std::cout << "\tquit                       -- exit" << std::endl;
      std::cout << "\tsleep <duration>           -- wait for the duration"
                << std::endl;
      std::cout << "\tget <region> <key>         -- get the value for a key"
                << std::endl;
      std::cout << "\tput <region> <key> <value> -- put the value for a key"
                << std::endl;
      std::cout << "\tinvalidate <region> <key>  -- invalidate the key"
                << std::endl;
      std::cout << "\tdestroy <region> <key>     -- destroy the key"
                << std::endl;
    }

    ++(*this);
  }
}

void Commands::parseLine(std::istream &is) {
  std::string line;
  if (std::getline(is, line)) {
    std::istringstream iss(line);
    while (iss) {
      std::string command;
      iss >> command;
      if (!command.empty()) {
        _commands.push_back(command);
      }
    }
  }
}
