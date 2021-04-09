
cmake_minimum_required(VERSION 3.4.1)

add_library(
        fly-opencv-native
        SHARED
        opencv_native_jni.cpp)


# 头文件的路径

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

# 引入库文件，设置内部的方式引入，指定库的目录是 -L  指定具体的库-l
# 这种方式方便快捷
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries( # Specifies the target library.
        fly-opencv-native
        log)

