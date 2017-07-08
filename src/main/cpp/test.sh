#!/bin/sh
# On Linux or Windows Subsystem for Linux
#
# Usage: sandbox cmd file_stdout file_stderr time_limit memory_limit stack_limit output_limit file_result

echo "Compileing..."
g++ -o sandbox sandbox.cpp
echo "Testing..."
[ -f "sandbox" ] && ./sandbox ./HelloWorld.exe out.out err.out 1000 1000 1000 1000 result.out
echo "Test finish."