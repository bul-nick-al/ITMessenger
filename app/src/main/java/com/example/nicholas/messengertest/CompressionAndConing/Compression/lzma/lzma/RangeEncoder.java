package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.OutputStream;

class RangeEncoder extends RangeCoding {
    private OutputStream outputStream;
    private long low = 0;
    private int range = -1;
    private int cacheSize = 1;
    private int cache = 0;

    RangeEncoder(OutputStream outputStream) {
        this.outputStream = outputStream;

        // initialize PROBABILITY_PRICES
        for (int i = NUM_BITS - 1; i >= 0; i--) {
            int start = 1 << (NUM_BITS - i - 1);
            int end = start << 1;

            for (int j = start; j < end; j++) {
                PROBABILITY_PRICES[j] = (i << NUM_BIT_PRICE_SHIFT_BITS) +
                        (((end - j) << NUM_BIT_PRICE_SHIFT_BITS) >>> (NUM_BITS - i - 1));
            }
        }
    }

    void flush() throws IOException {
        for (int i = 0; i < 5; i++) {
            shiftLow();
        }
        outputStream.flush();
    }

    // writes output byte if needed and shifts 'low' one byte left
    private void shiftLow() throws IOException {
        int lowHi = (int) (low >>> 32);
        if (lowHi != 0 || low < 0xFF000000L) {
            int temp = cache;
            do {
                outputStream.write(temp + lowHi);
                temp = 0xFF;
            } while (--cacheSize != 0);
            cache = (((int) low) >>> 24);
        }
        cacheSize++;
        low = (low & 0xFFFFFF) << 8;
    }

    void encodeDirectBits(int v, int numTotalBits) throws IOException {
        for (int i = numTotalBits - 1; i >= 0; i--) {
            range >>>= 1;
            if (((v >>> i) & 1) == 1) {
                low += range;
            }
            if ((range & RangeEncoder.TOP_MASK) == 0) {
                range <<= 8;
                shiftLow();
            }
        }
    }

    void encode(short[] probabilities, int index, int symbol) throws IOException {
        int probability = probabilities[index];
        int newBound = (range >>> NUM_BIT_MODEL_TOTAL_BITS) * probability;
        if (symbol == 0) {
            range = newBound;
            probabilities[index] = (short) (probability + ((BIT_MODEL_TOTAL - probability) >>> NUM_MOVE_BITS));
        } else {
            low += (newBound & 0xFFFFFFFFL);
            range -= newBound;
            probabilities[index] = (short) (probability - ((probability) >>> NUM_MOVE_BITS));
        }
        if ((range & TOP_MASK) == 0) {
            range <<= 8;
            shiftLow();
        }
    }
}
