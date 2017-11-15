package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class BinTreeEncoder {
    private final short[] MODELS;
    private final int NUM_BIT_LEVELS;

    BinTreeEncoder(int numBitLevels) {
        NUM_BIT_LEVELS = numBitLevels;
        MODELS = new short[1 << numBitLevels];
        RangeDecoder.initBitModels(MODELS);
    }

    void encode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int bitIndex = NUM_BIT_LEVELS; bitIndex != 0; ) {
            bitIndex--;
            int bit = (symbol >>> bitIndex) & 1;
            rangeEncoder.encode(MODELS, m, bit);
            m = (m << 1) | bit;
        }
    }

    void reverseEncode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i < NUM_BIT_LEVELS; i++) {
            int bit = symbol & 1;
            rangeEncoder.encode(MODELS, m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }

    int getPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int bitIndex = NUM_BIT_LEVELS; bitIndex != 0; ) {
            bitIndex--;
            int bit = (symbol >>> bitIndex) & 1;
            price += RangeEncoder.getPrice(MODELS[m], bit);
            m = (m << 1) + bit;
        }
        return price;
    }

    int reverseGetPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int i = NUM_BIT_LEVELS; i != 0; i--) {
            int bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.getPrice(MODELS[m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    static int reverseGetPrice(short[] Models, int startIndex,
                               int NumBitLevels, int symbol) {
        int price = 0;
        int m = 1;
        for (int i = NumBitLevels; i != 0; i--) {
            int bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.getPrice(Models[startIndex + m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    static void reverseEncode(short[] Models, int startIndex,
                              RangeEncoder rangeEncoder, int NumBitLevels, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i < NumBitLevels; i++) {
            int bit = symbol & 1;
            rangeEncoder.encode(Models, startIndex + m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }
}
