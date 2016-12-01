cmake_minimum_required(VERSION 3.4)
project(protobuf)

#set(CMAKE_CONFIGURATION_TYPES Debug Release)
set(CMAKE_CONFIGURATION_TYPES Debug)
if (NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Debug)
endif()

set_property(GLOBAL PROPERTY USE_FOLDERS ON)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(CheckCXXCompilerFlag)
include(CheckCCompilerFlag)

INCLUDE_DIRECTORIES(/usr/local/include)
LINK_DIRECTORIES(/usr/local/lib)

set(CMAKE_REQUIRED_LIBRARIES -m64)
check_c_compiler_flag(-m64 CFLAGS_M64_ALLOWED)
check_cxx_compiler_flag(-m64 CXXFLAGS_M64_ALLOWED)
set(CMAKE_REQUIRED_LIBRARIES)

check_c_compiler_flag(-mt CFLAGS_mt_ALLOWED)

if (CFLAGS_M64_ALLOWED AND CXXFLAGS_M64_ALLOWED)
  set(CMAKE_REQUIRED_FLAGS ${CMAKE_REQUIRED_FLAGS} -m64)
  add_compile_options(-m64)
  set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m64")
  set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} -m64")
  set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -m64")
endif()

if (CFLAGS_mt_ALLOWED)
  set(CMAKE_REQUIRED_FLAGS ${CMAKE_REQUIRED_FLAGS} -mt)
  add_compile_options(-mt)
endif()

set(CMAKE_VERBOSE_MAKEFILE 0)

add_library(c++11 INTERFACE)

find_package(Java 1.8.0.60 REQUIRED COMPONENTS Development)

if ("${CMAKE_AR}" STREQUAL "CMAKE_AR-NOTFOUND")
  message(FATAL_ERROR "Utility ar not found.")
endif()

if (WIN32 OR ${CMAKE_GENERATOR} STREQUAL "Xcode")
  set(CMAKE_USES_BUILDTYPE TRUE)
  set ( _DEBUG_OR_RELEASE $<$<CONFIG:Debug>:Debug>$<$<NOT:$<CONFIG:Debug>>:Release>)
else()
  set(CMAKE_USES_BUILDTYPE FALSE)
  set ( _DEBUG_OR_RELEASE )
endif()

# Since RPATH embeds path information into the executables that
# may not be valid in all execution contexts, do not use RPATH.
set(CMAKE_SKIP_RPATH TRUE)
set(CMAKE_MACOSX_RPATH FALSE)

# Default to only showing output on failure for unit tests but allow
# overriding with the CTEST_UNITTEST_VERBOSITY environment variable.
set(CTEST_UNITTEST_VERBOSITY --output-on-failure)
if(DEFINED ENV{CTEST_UNITTEST_VERBOSITY})
    set(CTEST_UNITTEST_VERBOSITY $ENV{CTEST_UNITTEST_VERBOSITY})
endif()

add_custom_target(generate-protobuf
  COMMAND protoc message.proto --cpp_out=cpp --java_out=java
  USES_TERMINAL)

add_subdirectory(cpp)
add_subdirectory(java)
