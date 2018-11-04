## Proxmark3 jansson build script.
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_library(jansson SHARED
        ${PM3_ROOT}/client/jansson/dump.c
        ${PM3_ROOT}/client/jansson/error.c
        ${PM3_ROOT}/client/jansson/hashtable.c
        ${PM3_ROOT}/client/jansson/hashtable_seed.c
        ${PM3_ROOT}/client/jansson/load.c
        ${PM3_ROOT}/client/jansson/memory.c
        ${PM3_ROOT}/client/jansson/pack_unpack.c
        ${PM3_ROOT}/client/jansson/strbuffer.c
        ${PM3_ROOT}/client/jansson/strconv.c
        ${PM3_ROOT}/client/jansson/utf.c
        ${PM3_ROOT}/client/jansson/value.c)

target_include_directories(jansson PRIVATE
        ${PM3_ROOT}/client/jansson)

target_compile_definitions(jansson PRIVATE
        HAVE_STDINT_H)
