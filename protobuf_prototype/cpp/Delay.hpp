#pragma once

#include <time.h>

#include <random>
#include <string>

class Delay : public timespec {
 public:
  Delay();

  operator bool() const;

  void operator()();

  double seconds() const;

  void reset();

  void randomize();

  void parse(const std::string& str);

 private:
  static const long tau;
  std::mt19937 _rng;
  std::uniform_int_distribution<std::mt19937::result_type> _distribution;
};
