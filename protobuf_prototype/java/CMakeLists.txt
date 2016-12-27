cmake_minimum_required (VERSION 2.8)
find_package(Java REQUIRED)
include(UseJava)
project (protobuf_prototype_java)

# Determine JAVA_HOME.
get_filename_component(JAVA_BIN ${Java_JAVA_EXECUTABLE} DIRECTORY)
get_filename_component(JAVA_HOME ${JAVA_BIN} DIRECTORY)

# Update the class path.
set(CMAKE_JAVA_INCLUDE_PATH ${JAVA_HOME}/lib/tools.jar:/usr/local/lib/protobuf-java-3.1.0.jar)

# Compile and jar the classes.
file(GLOB_RECURSE JAVA_SOURCES "*.java")
add_jar(protobuf_prototype_java ${JAVA_SOURCES} OUTPUT_NAME build/protobuf_prototype_java)
#add_dependencies(protobuf_prototype_java generate-protobuf)

add_custom_target(run_java_server
  COMMAND java -cp java/build/protobuf_prototype_java.jar:/usr/local/lib/protobuf-java-3.1.0.jar io.pivotal.Main
  WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
  USES_TERMINAL)
add_dependencies(run_java_server protobuf_prototype_java)
