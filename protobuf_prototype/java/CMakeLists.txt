cmake_minimum_required (VERSION 2.8)
find_package(Java REQUIRED)
include(UseJava)
project (server)

# Determine JAVA_HOME.
get_filename_component(JAVA_BIN ${Java_JAVA_EXECUTABLE} DIRECTORY)
get_filename_component(JAVA_HOME ${JAVA_BIN} DIRECTORY)

# Update the class path.
set(CMAKE_JAVA_INCLUDE_PATH ${JAVA_HOME}/lib/tools.jar:/usr/local/lib/protobuf-java-3.1.0.jar)

# Compile and jar the classes.
file(GLOB_RECURSE JAVA_SOURCES "*.java")
add_jar(server ${JAVA_SOURCES})
#add_dependencies(server generate-protobuf)
