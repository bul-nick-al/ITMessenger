package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class BinTreeDecoder {
    private final short[] MODELS;
    private final int NUM_BIT_LEVELS;

    BinTreeDecoder(int numBitLevels) {
        NUM_BIT_LEVELS = numBitLevels;
        MODELS = new short[1 << numBitLevels];
        RangeDecoder.initBitModels(MODELS);
    }

    int decode(RangeDecoder rangeDecoder) throws IOException {
        int m = 1;
        for (int bitIndex = NUM_BIT_LEVELS; bitIndex != 0; bitIndex--) {
            m = (m << 1) + rangeDecoder.decodeBit(MODELS, m);
        }
        return m - (1 << NUM_BIT_LEVELS);
    }

    int reverseDecode(RangeDecoder rangeDecoder) throws IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < NUM_BIT_LEVELS; bitIndex++) {
            int bit = rangeDecoder.decodeBit(MODELS, m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }

    static int reverseDecode(short[] Models, int startIndex,
                             RangeDecoder rangeDecoder, int NumBitLevels) throws IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
            int bit = rangeDecoder.decodeBit(Models, startIndex + m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }
}
