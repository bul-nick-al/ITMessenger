package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;

class LiteralSubDecoder {
    private final short[] decoders = new short[0x300];

    LiteralSubDecoder() {
        RangeDecoder.initBitModels(decoders);
    }

    byte decodeNormal(RangeDecoder rangeDecoder) throws IOException {
        int symbol = 1;
        do {
            symbol = (symbol << 1) | rangeDecoder.decodeBit(decoders, symbol);
        }
        while (symbol < 0x100);
        return (byte) symbol;
    }

    byte decodeWithMatchByte(RangeDecoder rangeDecoder, byte matchByte) throws IOException {
        int symbol = 1;
        do {
            int matchBit = (matchByte >> 7) & 1;
            matchByte <<= 1;
            int bit = rangeDecoder.decodeBit(decoders, ((1 + matchBit) << 8) + symbol);
            symbol = (symbol << 1) | bit;
            if (matchBit != bit) {
                while (symbol < 0x100) {
                    symbol = (symbol << 1) | rangeDecoder.decodeBit(decoders, symbol);
                }
                break;
            }
        }
        while (symbol < 0x100);
        return (byte) symbol;
    }
}
