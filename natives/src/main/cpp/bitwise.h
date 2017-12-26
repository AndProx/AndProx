
#include <stdint.h>

typedef struct {
    uint8_t * buffer;
    uint32_t numbits;
    uint32_t position;
} BitstreamIn;

bool headBit(BitstreamIn *stream);
