// bitwise.c
// License: GPLv2+
// Adapted from https://github.com/Proxmark/proxmark3/commit/f6d9fb173fec6d117faeb6c39cf37ee449d4ef16
// (cmddata.c)
// Author: Martin Holst Swende (2015)
//
// This contains bitwise operations that are otherwise used in loclass (which is GPLv2 only).

#include <stdbool.h>
#include "bitwise.h"

bool headBit(BitstreamIn *stream)
{
    int bytepos = stream->position >> 3; // divide by 8
    int bitpos = (stream->position++) & 7; // mask out 00000111
    return (*(stream->buffer + bytepos) >> (7-bitpos)) & 1;
}

