#include "Arguments.hpp"

#include <fstream>

Arguments::Arguments(const int argc, const char *argv[])
{
  for (int i = 1; i < argc; ++i) {
    std::ifstream ifs(argv[i]);
    if (!ifs) {
      _commands.push_back(argv[i]);
    }
    else {
      while (ifs) {
        parseLine(ifs);
      }
    }
  }
}

Arguments::operator bool()
{
  return !_commands.empty();
}
