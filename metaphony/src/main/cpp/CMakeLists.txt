cmake_minimum_required(VERSION 3.22.1)

project("metaphony")

add_subdirectory(taglib)

include_directories(taglib/taglib
        taglib/taglib/ape
        taglib/taglib/asf
        taglib/taglib/dsdiff
        taglib/taglib/dsf
        taglib/taglib/flac
        taglib/taglib/it
        taglib/taglib/mod
        taglib/taglib/mp4
        taglib/taglib/mpc
        taglib/taglib/mpeg
        taglib/taglib/mpeg/id3v1
        taglib/taglib/mpeg/id3v2
        taglib/taglib/mpeg/id3v2/frames
        taglib/taglib/ogg
        taglib/taglib/ogg/flac
        taglib/taglib/ogg/opus
        taglib/taglib/ogg/speex
        taglib/taglib/ogg/vorbis
        taglib/taglib/riff
        taglib/taglib/riff/aiff
        taglib/taglib/riff/wav
        taglib/taglib/s3m
        taglib/taglib/toolkit
        taglib/taglib/trueaudio
        taglib/taglib/wavpack
        taglib/taglib/xm)

add_library(${CMAKE_PROJECT_NAME} SHARED
        AudioMetadataParser.cpp
        TagLibHelper.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log
        tag)
