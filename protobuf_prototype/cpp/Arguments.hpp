#pragma once

#include "Commands.hpp"

class Arguments : public Commands {
 public:
  Arguments(const int argc, const char *argv[]);

  virtual operator bool();
};
