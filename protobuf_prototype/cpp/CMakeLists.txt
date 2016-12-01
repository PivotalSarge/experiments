cmake_minimum_required(VERSION 3.4)
project(client)

include_directories(/usr/local/include)

file(GLOB_RECURSE SOURCES "*.cpp" "*.cc")
add_executable(client.exe ${SOURCES})
#add_dependencies(client.exe generate-protobuf)
target_link_libraries(client.exe PUBLIC protobuf c++11)
