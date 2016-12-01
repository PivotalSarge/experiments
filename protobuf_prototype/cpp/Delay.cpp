#include "Delay.hpp"

#include <sstream>

const long Delay::tau = 500000000;

Delay::Delay() : _distribution(0, tau)
{
  _rng.seed(std::random_device()());
}

Delay::operator bool() const
{
  return (0 < tv_sec || 0 < tv_nsec);
}

void Delay::operator()()
{
  ::nanosleep(this, NULL);
}

double Delay::seconds() const
{
  return ((1.e9 * tv_sec) + tv_nsec) / 1.e9;
}

void Delay::reset()
{
  tv_sec = 0;
  tv_nsec = 0;
}

void Delay::randomize()
{
  tv_nsec = tau + _distribution(_rng);
}

void Delay::parse(const std::string& str)
{
  std::istringstream iss(str);
  tv_nsec = 0L;
  iss >> tv_nsec;
  std::string units;
  iss >> units;
  if (units == "s") {
    tv_nsec *= 1000000000;
  }
  else if (units == "u") {
    tv_nsec *= 1000;
  }
}
