package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class LiteralSubEncoder {
    private short[] encoders = new short[0x300];

    public void init() {
        RangeEncoder.initBitModels(encoders);
    }


    void encode(RangeEncoder rangeEncoder, byte symbol) throws IOException {
        int context = 1;
        for (int i = 7; i >= 0; i--) {
            int bit = ((symbol >> i) & 1);
            rangeEncoder.encode(encoders, context, bit);
            context = (context << 1) | bit;
        }
    }

    void encodeMatched(RangeEncoder rangeEncoder, byte matchByte, byte symbol) throws IOException {
        int context = 1;
        boolean same = true;
        for (int i = 7; i >= 0; i--) {
            int bit = ((symbol >> i) & 1);
            int state = context;
            if (same) {
                int matchBit = ((matchByte >> i) & 1);
                state += ((1 + matchBit) << 8);
                same = (matchBit == bit);
            }
            rangeEncoder.encode(encoders, state, bit);
            context = (context << 1) | bit;
        }
    }

    int getPrice(boolean matchMode, byte matchByte, byte symbol) {
        int price = 0;
        int context = 1;
        int i = 7;
        if (matchMode) {
            for (; i >= 0; i--) {
                int matchBit = (matchByte >> i) & 1;
                int bit = (symbol >> i) & 1;
                price += RangeEncoder.getPrice(encoders[((1 + matchBit) << 8) + context], bit);
                context = (context << 1) | bit;
                if (matchBit != bit) {
                    i--;
                    break;
                }
            }
        }
        for (; i >= 0; i--) {
            int bit = (symbol >> i) & 1;
            price += RangeEncoder.getPrice(encoders[context], bit);
            context = (context << 1) | bit;
        }
        return price;
    }
}
