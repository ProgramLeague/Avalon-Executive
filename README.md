# Avalon-Executive [![GNU Affero General Public License, version 3](https://www.gnu.org/graphics/agplv3-155x51.png)](https://www.gnu.org/licenses/lgpl.html)
A safe program compiler & executor base on Docker.

## Usage
1. Prepare environment and build C++ part: 

    On Linux (**Not test**):
    1. Run commands:  
    ```
    sudo apt-get install g++ docker
    sudo docker -H 127.0.0.1:2375 -d &
    cd SOURCE_CODE_PATH/src/main/cpp
    sudo ./test.sh
    mv sandbox SOURCE_CODE_PATH
    ```
    
    On Windows 10 **Pro**, **Enterprise** or **Education**, **build >= 10586**:
    1. Download and install [Docker for Windows](https://www.docker.com/docker-windows).
    2. Enable "Expose daemon on tcp://localhost:2375 without TLS" in setting of Docker.
    3. Download and install [Cygwin](https://www.cygwin.com).
    4. Install Pending packages, and package `gcc-core`, `gcc-g++`.
    5. Run commands **in Cygwin**:
    ```
    cd SOURCE_CODE_PATH/src/main/cpp
    g++ -o sandbox sandbox.cpp
    mv sandbox SOURCE_CODE_PATH
    ```
    
2. Download newest RELEASE. (If no release, see *Build*)
3. Execute file.

## Build
1. Build C++ part. (See *Usage / Prepare environment and build C++ part*)
2. Build / Execute Java / Scala part. (`ray.eldath.avalon.executive.main.MainServer`)