## This file builds Proxmark3's client "as a library".
## It is necessary to build this with cmake rather than other tools for ease of use with the rest of
## Android's toolchain.
##
## mainline proxmark3 repository: https://github.com/proxmark/proxmark3
cmake_minimum_required(VERSION 3.4.1)

get_filename_component(PM3_ROOT ../../../../../third_party/proxmark3 ABSOLUTE)
message(STATUS "Proxmark3 root = ${PM3_ROOT}")

get_filename_component(APN_ROOT ../../cpp ABSOLUTE)
message(STATUS "AndProx natives root = ${APN_ROOT}")

get_filename_component(CMAKE_COMMON_ROOT ../common ABSOLUTE)
message(STATUS "CMake common root = ${CMAKE_COMMON_ROOT}")

find_package(Git)

## Get PM3 version
if(GIT_FOUND)
    execute_process(
        COMMAND ${GIT_EXECUTABLE} describe --dirty
        WORKING_DIRECTORY "${PM3_ROOT}"
        RESULT_VARIABLE PM3_GIT_DESCRIBE_RES
        OUTPUT_VARIABLE PM3_GIT_DESCRIBE
        ERROR_VARIABLE PM3_ERROR
        OUTPUT_STRIP_TRAILING_WHITESPACE)
    if(NOT ${PM3_GIT_DESCRIBE_RES} EQUAL 0)
        message(FATAL_ERROR "git describe failed ${PM3_GIT_DESCRIBE_RES}: ${PM3_ERROR}")

    endif()
    execute_process(
        COMMAND ${GIT_EXECUTABLE} rev-parse --abbrev-ref HEAD
        WORKING_DIRECTORY "${PM3_ROOT}"
        RESULT_VARIABLE PM3_GIT_REVPARSE_RES
        OUTPUT_VARIABLE PM3_GIT_REVPARSE
        ERROR_VARIABLE PM3_ERROR
        OUTPUT_STRIP_TRAILING_WHITESPACE)
    if(NOT ${PM3_GIT_REVPARSE_RES} EQUAL 0)
        message(FATAL_ERROR "git rev-parse failed ${PM3_GIT_REVPARSE_RES}: ${PM3_ERROR}")
    endif()

    set(PM3_GIT_VER "${PM3_GIT_DESCRIBE}/${PM3_GIT_REVPARSE}")
else()
    set(PM3_GIT_VER "unknown-version")
endif()

message(STATUS "PM3_GIT_VER := ${PM3_GIT_VER}")

string(TIMESTAMP PM3_TS UTC)
message(STATUS "PM3_TS := ${PM3_TS}")

## CPU-specific code
## These are mostly for x86-based architectures, which is not useful for many Android devices.
add_library(hardnested_nosimd OBJECT
            ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
            ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

target_include_directories(hardnested_nosimd PRIVATE
                           ${PM3_ROOT}/common
                           ${PM3_ROOT}/client
                           ${PM3_ROOT}/client/hardnested)
set(X86_CPUS x86 x86_64 i686)
message(STATUS "CMAKE_SYSTEM_PROCESSOR := ${CMAKE_SYSTEM_PROCESSOR}")
if("${CMAKE_SYSTEM_PROCESSOR}" IN_LIST X86_CPUS)
    message(STATUS "Building optimised x86/x86_64 binaries")
    target_compile_options(hardnested_nosimd BEFORE PRIVATE
                           -mno-mmx -mno-sse2 -mno-avx -mno-avx2 -mno-avx512f)

    ## x86 / MMX
    add_library(hardnested_mmx OBJECT
                ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
                ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_mmx BEFORE PRIVATE
                           -mmmx -mno-sse2 -mno-avx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_mmx PRIVATE
                               ${PM3_ROOT}/common
                               ${PM3_ROOT}/client
                               ${PM3_ROOT}/client/hardnested)

    ## x86 / SSE2
    add_library(hardnested_sse2 OBJECT
                ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
                ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_sse2 BEFORE PRIVATE
                           -mmmx -msse2 -mno-avx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_sse2 PRIVATE
                               ${PM3_ROOT}/common
                               ${PM3_ROOT}/client
                               ${PM3_ROOT}/client/hardnested)

    ## x86 / AVX
    add_library(hardnested_avx OBJECT
                ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
                ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx BEFORE PRIVATE
                           -mmmx -msse2 -mavx -mno-avx2 -mno-avx512f)

    target_include_directories(hardnested_avx PRIVATE
                               ${PM3_ROOT}/common
                               ${PM3_ROOT}/client
                               ${PM3_ROOT}/client/hardnested)

    ## x86 / AVX2
    add_library(hardnested_avx2 OBJECT
                ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
                ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx2 BEFORE PRIVATE
                           -mmmx -msse2 -mavx -mavx2 -mno-avx512f)

    target_include_directories(hardnested_avx2 PRIVATE
                               ${PM3_ROOT}/common
                               ${PM3_ROOT}/client
                               ${PM3_ROOT}/client/hardnested)

    ## x86 / AVX512
    add_library(hardnested_avx512 OBJECT
                ${PM3_ROOT}/client/hardnested/hardnested_bf_core.c
                ${PM3_ROOT}/client/hardnested/hardnested_bitarray_core.c)

    target_compile_options(hardnested_avx512 BEFORE PRIVATE
                           -mmmx -msse2 -mavx -mavx2 -mavx512f)

    target_include_directories(hardnested_avx512 PRIVATE
                               ${PM3_ROOT}/common
                               ${PM3_ROOT}/client
                               ${PM3_ROOT}/client/hardnested)
    set(SIMD_TARGETS
        $<TARGET_OBJECTS:hardnested_mmx>
        $<TARGET_OBJECTS:hardnested_sse2>
        $<TARGET_OBJECTS:hardnested_avx>
        $<TARGET_OBJECTS:hardnested_avx2>
        $<TARGET_OBJECTS:hardnested_avx512>)
else()
    message(STATUS "Not building optimised targets")
    set(SIMD_TARGETS)
endif()

include("${CMAKE_COMMON_ROOT}/liblua.cmake")

add_library(natives SHARED
            ${APN_ROOT}/natives.c
            ${APN_ROOT}/fakemain.c
            ${APN_ROOT}/uart_android.c

            ${PM3_ROOT}/client/guidummy.cpp
            ${PM3_ROOT}/client/ui.c
            ${PM3_ROOT}/client/util.c
            ${PM3_ROOT}/client/util_posix.c
            ${PM3_ROOT}/client/comms.c
            ${PM3_ROOT}/client/data.c

            ${PM3_ROOT}/common/crapto1/crapto1.c
            ${PM3_ROOT}/common/crapto1/crypto1.c
            ${PM3_ROOT}/common/polarssl/des.c
            ${PM3_ROOT}/common/polarssl/aes.c
            ${PM3_ROOT}/common/polarssl/bignum.c
            ${PM3_ROOT}/common/polarssl/rsa.c
            ${PM3_ROOT}/common/polarssl/sha1.c
            ${PM3_ROOT}/client/mfkey.c
            ${PM3_ROOT}/client/loclass/cipher.c
            ${PM3_ROOT}/client/loclass/cipherutils.c
            ${PM3_ROOT}/client/loclass/ikeys.c
            ${PM3_ROOT}/client/loclass/elite_crack.c
            ${PM3_ROOT}/client/loclass/fileutils.c

            ${PM3_ROOT}/client/mifarehost.c
            ${PM3_ROOT}/common/parity.c
            ${PM3_ROOT}/common/crc.c
            ${PM3_ROOT}/common/crc16.c
            ${PM3_ROOT}/common/crc64.c
            ${PM3_ROOT}/common/iso14443crc.c
            ${PM3_ROOT}/common/iso15693tools.c
            ${PM3_ROOT}/client/graph.c
            ${PM3_ROOT}/client/cmddata.c
            ${PM3_ROOT}/common/lfdemod.c

            ${PM3_ROOT}/client/emv/crypto_polarssl.c
            ${PM3_ROOT}/client/emv/crypto.c
            ${PM3_ROOT}/client/emv/emv_pk.c
            ${PM3_ROOT}/client/emv/emv_pki.c
            ${PM3_ROOT}/client/emv/emv_pki_priv.c
            ${PM3_ROOT}/client/emv/test/cryptotest.c
            ${PM3_ROOT}/client/emv/apduinfo.c
            ${PM3_ROOT}/client/emv/dump.c
            ${PM3_ROOT}/client/emv/tlv.c
            ${PM3_ROOT}/client/emv/emv_tags.c
            ${PM3_ROOT}/client/emv/dol.c
            ${PM3_ROOT}/client/emv/emvcore.c
            ${PM3_ROOT}/client/emv/test/crypto_test.c
            ${PM3_ROOT}/client/emv/test/sda_test.c
            ${PM3_ROOT}/client/emv/test/dda_test.c
            ${PM3_ROOT}/client/emv/test/cda_test.c
            ${PM3_ROOT}/client/emv/cmdemv.c

            ${PM3_ROOT}/client/cmdhf.c
            ${PM3_ROOT}/client/cmdhflist.c
            ${PM3_ROOT}/client/cmdhf14a.c
            ${PM3_ROOT}/client/cmdhf14b.c
            ${PM3_ROOT}/client/cmdhf15.c
            ${PM3_ROOT}/client/cmdhfepa.c
            ${PM3_ROOT}/client/cmdhflegic.c
            ${PM3_ROOT}/client/cmdhficlass.c
            ${PM3_ROOT}/client/cmdhfmf.c
            ${PM3_ROOT}/client/cmdhfmfu.c
            ${PM3_ROOT}/client/cmdhfmfhard.c
            ${PM3_ROOT}/client/hardnested/hardnested_bruteforce.c
            ${PM3_ROOT}/client/cmdhftopaz.c

            ${PM3_ROOT}/client/cmdhw.c

            ${PM3_ROOT}/client/cmdlf.c
            ${PM3_ROOT}/client/cmdlfawid.c
            ${PM3_ROOT}/client/cmdlfcotag.c
            ${PM3_ROOT}/client/cmdlfem4x.c
            ${PM3_ROOT}/client/cmdlffdx.c
            ${PM3_ROOT}/client/cmdlfgproxii.c
            ${PM3_ROOT}/client/cmdlfhid.c
            ${PM3_ROOT}/client/cmdlfhitag.c
            ${PM3_ROOT}/client/cmdlfio.c
            ${PM3_ROOT}/client/cmdlfindala.c
            ${PM3_ROOT}/client/cmdlfjablotron.c
            ${PM3_ROOT}/client/cmdlfnexwatch.c
            ${PM3_ROOT}/client/cmdlfnoralsy.c
            ${PM3_ROOT}/client/cmdlfparadox.c
            ${PM3_ROOT}/client/cmdlfpcf7931.c
            ${PM3_ROOT}/client/cmdlfpresco.c
            ${PM3_ROOT}/client/cmdlfpyramid.c
            ${PM3_ROOT}/client/cmdlfsecurakey.c
            ${PM3_ROOT}/client/cmdlft55xx.c
            ${PM3_ROOT}/client/cmdlfti.c
            ${PM3_ROOT}/client/cmdlfviking.c
            ${PM3_ROOT}/client/cmdlfvisa2000.c
            ${PM3_ROOT}/client/cmdlfpac.c
            ${PM3_ROOT}/client/cmdparser.c
            ${PM3_ROOT}/client/cmdmain.c

            ${PM3_ROOT}/common/protocols.c

            ${APN_ROOT}/scripting_android.c
            ${PM3_ROOT}/client/cmdscript.c
            ${PM3_ROOT}/client/pm3_binlib.c
            ${PM3_ROOT}/client/pm3_bitlib.c

            $<TARGET_OBJECTS:hardnested_nosimd>
            ${SIMD_TARGETS}
            $<TARGET_OBJECTS:liblua>
)

#             ${APN_ROOT}/fakecmdscript.c

# Proxmark3's version of zlib: (disabled)
#            ${PM3_ROOT}/zlib/deflate.c
#            ${PM3_ROOT}/zlib/adler32.c
#            ${PM3_ROOT}/zlib/trees.c
#            ${PM3_ROOT}/zlib/zutil.c
#            ${PM3_ROOT}/zlib/inflate.c
#            ${PM3_ROOT}/zlib/inffast.c
#            ${PM3_ROOT}/zlib/inftrees.c

## General compile options.
target_include_directories(natives PRIVATE
                           ${APN_ROOT}
                           ${PM3_ROOT}/client
                           ${PM3_ROOT}/common
                           ${PM3_ROOT}/common/polarssl
                           ${PM3_ROOT}/include
                           ${PM3_ROOT}/client/nonce2key
                           ${PM3_ROOT}/client/hardnested
                           ${PM3_ROOT}/lua_android
                           ${PM3_ROOT}/liblua
                           ${PM3_ROOT}/uart)


target_compile_definitions(natives PRIVATE
    EXTERNAL_PRINTANDLOG
    PM3_GIT_VER="${PM3_GIT_VER}"
    PM3_TS="${PM3_TS}")

# Defines needed for Proxmark3's version of zlib: (disabled)
#    Z_SOLO
#    Z_PREFIX
#    NO_GZIP
#    ZLIB_PM3_TUNED

target_link_libraries(natives
                      android log z)
