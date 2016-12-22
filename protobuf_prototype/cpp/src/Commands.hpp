#pragma once

#include <iostream>
#include <list>

class Commands {
 public:
  virtual ~Commands();

  virtual operator bool() = 0;

  virtual const std::string &operator*() const;

  virtual Commands &operator++();

 protected:
  std::list<std::string> _commands;

  Commands();

  void parseLine(std::istream &is);
};
