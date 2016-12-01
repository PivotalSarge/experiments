#include "Shell.hpp"

#include <sstream>

Shell::Shell() : _is(std::cin)
{
  // NOP
}

Shell::operator bool()
{
  if (!_commands.empty()) {
    return true;
  }

  std::cout << "> ";
  parseLine(_is);
  return (bool)_is;
}
