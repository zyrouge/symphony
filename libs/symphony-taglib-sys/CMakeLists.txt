cmake_minimum_required(VERSION 3.20)

project(symphony_taglib_sys C CXX)

set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
set(BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(BUILD_BINDINGS ON CACHE BOOL "" FORCE)
set(WITH_ZLIB ON CACHE BOOL "" FORCE)

set(
    utf8cpp_INCLUDE_DIR
    "${CMAKE_CURRENT_SOURCE_DIR}/vendor/utfcpp/source"
    CACHE PATH "" FORCE
)

set(
    ZLIB_ROOT
    "${CMAKE_CURRENT_SOURCE_DIR}/vendor/zlib"
    CACHE PATH "" FORCE
)

add_subdirectory(
    "${CMAKE_CURRENT_SOURCE_DIR}/vendor/taglib"
    "${CMAKE_CURRENT_BINARY_DIR}/taglib"
)

target_sources(
    tag_c PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/cpp/src/taglib_fd_iostream.cpp"
)

target_include_directories(
    tag_c PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/cpp/include"
)

install(
    TARGETS tag tag_c
    RUNTIME DESTINATION bin
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)
