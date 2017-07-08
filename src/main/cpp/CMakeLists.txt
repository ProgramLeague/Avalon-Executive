cmake_minimum_required(VERSION 3.7)
project(cpp)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES sandbox.cpp)
add_executable(cpp ${SOURCE_FILES})
include_directories(D:\\cygwin64\\usr\\include)