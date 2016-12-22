#pragma once

#include "Commands.hpp"

class Shell : public Commands {
 public:
  Shell();

  virtual operator bool();

 private:
  std::istream &_is;
};
