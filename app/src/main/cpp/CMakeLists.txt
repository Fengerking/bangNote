cmake_minimum_required(VERSION 3.4.1)

#将静态库的头文件添加进来
include_directories(
        ${CMAKE_SOURCE_DIR}/include
        ${CMAKE_SOURCE_DIR}/src
        ${CMAKE_SOURCE_DIR}/src/external
        ${CMAKE_SOURCE_DIR}/include/alibabacloud/oss)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
            native-lib.cpp
            COSSMng.cpp)


#添加自己所需的静态库 库多的话，可以使用下面的方式添加
add_library(bbaoss          #库名字 去掉了 lib 与 .a
            STATIC          #必须的
            IMPORTED        #必须的
        )
set_target_properties(
        bbaoss
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libbbaoss.a)
add_library(crypto
        STATIC
        IMPORTED
        )
set_target_properties(
        crypto
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libcrypto.a)
add_library(ssl
        STATIC
        IMPORTED
        )
set_target_properties(
        ssl
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libssl.a)
add_library(curl
        STATIC
        IMPORTED
        )
set_target_properties(
        curl
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libcurl.a)

find_library( # Sets the name of the path variable.
              log-lib
              log )
find_library(
        z-lib
        z )

target_link_libraries( # Specifies the target library.
                       native-lib bbaoss curl ssl crypto

                       ${log-lib}
                        ${z-lib})