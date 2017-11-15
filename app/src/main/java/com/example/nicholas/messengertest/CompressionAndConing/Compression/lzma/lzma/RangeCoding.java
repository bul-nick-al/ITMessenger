package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

abstract class RangeCoding {
    static final int TOP_MASK = ~((1 << 24) - 1);

    // ... "context", which consists of the unsigned 11-bit variable prob...
    static final int NUM_BIT_MODEL_TOTAL_BITS = 11;

    // number of possible bit models
    static final int BIT_MODEL_TOTAL = (1 << NUM_BIT_MODEL_TOTAL_BITS);

    static final int NUM_MOVE_BITS = 5;
    private static final int NUM_MOVE_REDUCING_BITS = 2;
    static final int[] PROBABILITY_PRICES = new int[BIT_MODEL_TOTAL >>> NUM_MOVE_REDUCING_BITS];
    static final int NUM_BITS = (NUM_BIT_MODEL_TOTAL_BITS - NUM_MOVE_REDUCING_BITS);
    static final int NUM_BIT_PRICE_SHIFT_BITS = 6;

    static int getPrice(int prob, int symbol) {
        return PROBABILITY_PRICES[(((prob - symbol) ^ ((-symbol))) & (BIT_MODEL_TOTAL - 1)) >>> NUM_MOVE_REDUCING_BITS];
    }

    static int getPrice0(int prob) {
        return PROBABILITY_PRICES[prob >>> NUM_MOVE_REDUCING_BITS];
    }

    static int getPrice1(int prob) {
        return PROBABILITY_PRICES[(BIT_MODEL_TOTAL - prob) >>> NUM_MOVE_REDUCING_BITS];
    }

    public static void initBitModels(short[] probabilities) {
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = (BIT_MODEL_TOTAL >>> 1);
        }
    }
}
