#include "Commands.hpp"

#include <sstream>

Commands::Commands()
{
  // NOP
}

Commands::~Commands()
{
  // NOP
}

const std::string &Commands::operator*() const
{
  if (!_commands.empty()) {
    return _commands.front();
  }
  static const std::string none;
  return none;
}

Commands &Commands::operator++()
{
  if (!_commands.empty()) {
    _commands.pop_front();
  }
  return *this;
}

void Commands::parseLine(std::istream &is)
{
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
