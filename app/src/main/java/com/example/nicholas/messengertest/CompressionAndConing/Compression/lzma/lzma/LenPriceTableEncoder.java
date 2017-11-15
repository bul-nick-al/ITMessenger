package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class LenPriceTableEncoder extends LenEncoder {
    private int[] prices = new int[LZMACommon.NUM_LEN_SYMBOLS << LZMACommon.NUM_POS_STATES_BITS_ENCODING_MAX];
    private int tableSize;
    private int[] counters = new int[LZMACommon.NUM_POS_STATES_ENCODING_MAX];

    LenPriceTableEncoder(int numPosStates) {
        super(numPosStates);
    }

    void setTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    int getPrice(int symbol, int posState) {
        return prices[posState * LZMACommon.NUM_LEN_SYMBOLS + symbol];
    }

    private void updateTable(int posState) {
        setPrices(posState, tableSize, prices, posState * LZMACommon.NUM_LEN_SYMBOLS);
        counters[posState] = tableSize;
    }

    void updateTables(int numPosStates) {
        for (int posState = 0; posState < numPosStates; posState++) {
            updateTable(posState);
        }
    }

    public void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        super.encode(rangeEncoder, symbol, posState);
        if (--counters[posState] == 0) {
            updateTable(posState);
        }
    }
}
