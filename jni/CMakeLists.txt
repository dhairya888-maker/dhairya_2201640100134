cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

project(edge_pipeline)

add_library(edge_pipeline SHARED
    src/native_pipeline.cpp
    src/opencv_processor.cpp
    src/gl_texture_uploader.cpp
)

find_package(OpenCV REQUIRED core imgproc)

target_include_directories(edge_pipeline
    PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)

target_link_libraries(edge_pipeline
    android
    log
    GLESv2
    ${OpenCV_LIBS}
)

