package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.InputStream;

/**
 * Range encoding class
 * https://en.wikipedia.org/wiki/Range_encoding
 */
class RangeDecoder extends RangeCoding {
    private int range = -1;
    private int code = 0;

    private InputStream inputStream;

    RangeDecoder(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;

        // reads a number from the stream for reasons unknown
        for (int i = 0; i < 5; i++) {
            // TODO constant parameters
            code = (code << 8) | this.inputStream.read();
        }
    }

    int decodeDirectBits(int numTotalBits) throws IOException {
        int result = 0;
        for (int i = numTotalBits; i != 0; i--) {
            range >>>= 1;
            int t = ((code - range) >>> 31);
            code -= range & (t - 1);
            result = (result << 1) | (1 - t);

            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | inputStream.read();
                range <<= 8;
            }
        }
        return result;
    }

    int decodeBit(short[] probabilities, int index) throws IOException {
        int probability = probabilities[index];
        int newBound = (range >>> NUM_BIT_MODEL_TOTAL_BITS) * probability;
        if ((code ^ 0x80000000) < (newBound ^ 0x80000000)) {
            range = newBound;
            probabilities[index] = (short) (probability + ((BIT_MODEL_TOTAL - probability) >>> NUM_MOVE_BITS));
            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | inputStream.read();
                range <<= 8;
            }
            return 0;
        } else {
            range -= newBound;
            code -= newBound;
            probabilities[index] = (short) (probability - ((probability) >>> NUM_MOVE_BITS));
            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | inputStream.read();
                range <<= 8;
            }
            return 1;
        }
    }

    public static void initBitModels(short[] probabilities) {
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = (BIT_MODEL_TOTAL >>> 1);
        }
    }
}
