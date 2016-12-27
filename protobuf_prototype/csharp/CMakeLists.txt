cmake_minimum_required(VERSION 3.4)
project(protobuf_prototype_csharp)

add_custom_target(run_csharp_client
  COMMAND echo run_csharp_client
  WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
  USES_TERMINAL)
#add_dependencies(run_csharp_client protobuf_prototype_csharp.exe)
