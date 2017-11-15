package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

class LiteralEncoder {
    private LiteralSubEncoder[] coders;
    private int numPrevBits;
    private int numPosBits;
    private int posMask;


    LiteralEncoder(int numPosBits, int numPrevBits) {
        if (coders != null && this.numPrevBits == numPrevBits && this.numPosBits == numPosBits) {
            return;
        }
        this.numPosBits = numPosBits;
        posMask = (1 << numPosBits) - 1;
        this.numPrevBits = numPrevBits;
        int numStates = 1 << (this.numPrevBits + this.numPosBits);
        coders = new LiteralSubEncoder[numStates];
        for (int i = 0; i < numStates; i++) {
            coders[i] = new LiteralSubEncoder();
        }
    }

    public void init() {
        int numStates = 1 << (numPrevBits + numPosBits);
        for (int i = 0; i < numStates; i++) {
            coders[i].init();
        }
    }

    LiteralSubEncoder getSubCoder(int pos, byte prevByte) {
        return coders[((pos & posMask) << numPrevBits) + ((prevByte & 0xFF) >>> (8 - numPrevBits))];
    }
}
