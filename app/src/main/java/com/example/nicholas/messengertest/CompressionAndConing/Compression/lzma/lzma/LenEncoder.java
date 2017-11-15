package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class LenEncoder {
    private short[] choice = new short[2];
    private BinTreeEncoder[] lowCoder = new BinTreeEncoder[LZMACommon.NUM_POS_STATES_ENCODING_MAX];
    private BinTreeEncoder[] midCoder = new BinTreeEncoder[LZMACommon.NUM_POS_STATES_ENCODING_MAX];
    private BinTreeEncoder highCoder = new BinTreeEncoder(LZMACommon.NUM_HIGH_LEN_BITS);

    LenEncoder(int numPosStates) {
        for (int posState = 0; posState < LZMACommon.NUM_POS_STATES_ENCODING_MAX; posState++) {
            lowCoder[posState] = new BinTreeEncoder(LZMACommon.NUM_LOW_LEN_BITS);
            midCoder[posState] = new BinTreeEncoder(LZMACommon.NUM_MID_LEN_BITS);
        }
        RangeEncoder.initBitModels(choice);
    }

    public void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        if (symbol < LZMACommon.NUM_LOW_LEN_SYMBOLS) {
            rangeEncoder.encode(choice, 0, 0);
            lowCoder[posState].encode(rangeEncoder, symbol);
        } else {
            symbol -= LZMACommon.NUM_LOW_LEN_SYMBOLS;
            rangeEncoder.encode(choice, 0, 1);
            if (symbol < LZMACommon.NUM_MID_LEN_SYMBOLS) {
                rangeEncoder.encode(choice, 1, 0);
                midCoder[posState].encode(rangeEncoder, symbol);
            } else {
                rangeEncoder.encode(choice, 1, 1);
                highCoder.encode(rangeEncoder, symbol - LZMACommon.NUM_MID_LEN_SYMBOLS);
            }
        }
    }

    void setPrices(int posState, int numSymbols, int[] prices, int st) {
        int a0 = RangeEncoder.getPrice0(choice[0]);
        int a1 = RangeEncoder.getPrice1(choice[0]);
        int b0 = a1 + RangeEncoder.getPrice0(choice[1]);
        int b1 = a1 + RangeEncoder.getPrice1(choice[1]);
        int i;
        for (i = 0; i < LZMACommon.NUM_LOW_LEN_SYMBOLS; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = a0 + lowCoder[posState].getPrice(i);
        }
        for (; i < LZMACommon.NUM_LOW_LEN_SYMBOLS + LZMACommon.NUM_MID_LEN_SYMBOLS; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = b0 + midCoder[posState].getPrice(i - LZMACommon.NUM_LOW_LEN_SYMBOLS);
        }
        for (; i < numSymbols; i++) {
            prices[st + i] = b1 + highCoder.getPrice(i - LZMACommon.NUM_LOW_LEN_SYMBOLS - LZMACommon.NUM_MID_LEN_SYMBOLS);
        }
    }
}
