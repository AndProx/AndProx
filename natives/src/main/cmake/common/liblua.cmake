## Proxmark3 liblua build script.
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_library(liblua OBJECT
            ${PM3_ROOT}/liblua/lapi.c
            ${PM3_ROOT}/liblua/lcode.c
            ${PM3_ROOT}/liblua/lctype.c
            ${PM3_ROOT}/liblua/ldebug.c
            ${PM3_ROOT}/liblua/ldo.c
            ${PM3_ROOT}/liblua/ldump.c
            ${PM3_ROOT}/liblua/lfunc.c
            ${PM3_ROOT}/liblua/lgc.c
            ${PM3_ROOT}/liblua/llex.c
            ${PM3_ROOT}/liblua/lmem.c
            ${PM3_ROOT}/liblua/lobject.c
            ${PM3_ROOT}/liblua/lopcodes.c
            ${PM3_ROOT}/liblua/lparser.c
            ${PM3_ROOT}/liblua/lstate.c
            ${PM3_ROOT}/liblua/lstring.c
            ${PM3_ROOT}/liblua/ltable.c
            ${PM3_ROOT}/liblua/ltm.c
            ${PM3_ROOT}/liblua/lundump.c
            ${PM3_ROOT}/liblua/lvm.c
            ${PM3_ROOT}/liblua/lzio.c
            ${PM3_ROOT}/liblua/lauxlib.c
            ${PM3_ROOT}/liblua/lbaselib.c
            ${PM3_ROOT}/liblua/lbitlib.c
            ${PM3_ROOT}/liblua/lcorolib.c
            ${PM3_ROOT}/liblua/ldblib.c
            ${PM3_ROOT}/liblua/liolib.c
            ${PM3_ROOT}/liblua/lmathlib.c
            ${PM3_ROOT}/liblua/loslib.c
            ${PM3_ROOT}/liblua/lstrlib.c
            ${PM3_ROOT}/liblua/ltablib.c
            ${PM3_ROOT}/liblua/loadlib.c
            ${PM3_ROOT}/liblua/linit.c)

target_include_directories(liblua PRIVATE
                           ${PM3_ROOT}/lua_android
                           ${PM3_ROOT}/liblua)

## Mostly use Linux stuff, but Android has no f{tell,seek}o.
target_compile_definitions(liblua PRIVATE
    LUA_USE_LINUX
    fseeko=fseek
    ftello=ftell)
