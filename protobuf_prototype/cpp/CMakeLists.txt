cmake_minimum_required(VERSION 3.4)
project(protobuf_prototype_cplusplus)

include_directories(/usr/local/include)

file(GLOB_RECURSE SOURCES "src/*.cpp" "src/*.cc")
add_executable(protobuf_prototype_cplusplus.exe ${SOURCES})
#add_dependencies(protobuf_prototype_cplusplus.exe generate-protobuf)
# TODO SARGE target_link_libraries(test PUBLIC ${PROTOBUF_LIBRARY})
#target_link_libraries(protobuf_prototype_cplusplus.exe PUBLIC protobuf c++11)
target_link_libraries(protobuf_prototype_cplusplus.exe PUBLIC ${PROTOBUF_LIBRARY} c++11)

add_custom_target(run_cpp_client
  COMMAND cpp/protobuf_prototype_cplusplus.exe
  WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
  USES_TERMINAL)
add_dependencies(run_cpp_client protobuf_prototype_cplusplus.exe)
