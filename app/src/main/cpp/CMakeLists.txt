# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)


# 指定你的 C++ 源文件
add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp )

# 包含 Beauty SDK 头文件的目录
include_directories(${CMAKE_SOURCE_DIR}/inc)

# 链接 Beauty SDK 库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# 指定库的搜索路径
link_directories(src/main/jni/${ANDROID_ABI})

# 链接到你的第三方库
add_library( # Sets the name of the library.
        libarcsoft_beautyshot

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your prebuilt library.
        IMPORTED )

# 指定导入库的具体位置
set(libs "${CMAKE_SOURCE_DIR}/../jni")
set_target_properties(libarcsoft_beautyshot PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libarcsoft_beautyshot.so")



# 链接到你的第三方库
add_library( # Sets the name of the library.
        libmpbase

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your prebuilt library.
        IMPORTED )

# 指定导入库的具体位置
set(libs "${CMAKE_SOURCE_DIR}/../jni")
set_target_properties(libmpbase PROPERTIES
        IMPORTED_LOCATION "${libs}/platform-lib/libmpbase.so")



# 将库链接到你的 native-lib 库
target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        # Links the native-lib library to the arcsoft_beautyshot library.
        libarcsoft_beautyshot

        libmpbase

        )
