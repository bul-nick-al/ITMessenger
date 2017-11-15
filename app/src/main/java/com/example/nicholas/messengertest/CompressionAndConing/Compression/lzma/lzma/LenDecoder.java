package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class LenDecoder {
    private final short[] choice = new short[2];
    private final BinTreeDecoder[] lowCoder = new BinTreeDecoder[LZMACommon.NUM_POS_STATES_MAX];
    private final BinTreeDecoder[] midCoder = new BinTreeDecoder[LZMACommon.NUM_POS_STATES_MAX];
    private final BinTreeDecoder highCoder = new BinTreeDecoder(LZMACommon.NUM_HIGH_LEN_BITS);


    LenDecoder(int numPosStates) {
        for (int i = 0; i < numPosStates; i++) {
            lowCoder[i] = new BinTreeDecoder(LZMACommon.NUM_LOW_LEN_BITS);
            midCoder[i] = new BinTreeDecoder(LZMACommon.NUM_MID_LEN_BITS);
        }
        RangeDecoder.initBitModels(choice);
    }

    int decode(RangeDecoder rangeDecoder, int posState) throws IOException {
        if (rangeDecoder.decodeBit(choice, 0) == 0) {
            return lowCoder[posState].decode(rangeDecoder);
        }
        int symbol = LZMACommon.NUM_LOW_LEN_SYMBOLS;
        if (rangeDecoder.decodeBit(choice, 1) == 0) {
            symbol += midCoder[posState].decode(rangeDecoder);
        } else {
            symbol += LZMACommon.NUM_MID_LEN_SYMBOLS + highCoder.decode(rangeDecoder);
        }
        return symbol;
    }
}
