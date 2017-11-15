package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LZMADecoder {
    private InputStream inputStream;
    private OutputStream outputStream;
    private long outputSize;
    private final short[] isMatchDecoders = new short[LZMACommon.NUM_STATES << LZMACommon.NUM_POS_STATES_BITS_MAX];
    private final short[] isRepDecoders = new short[LZMACommon.NUM_STATES];
    private final short[] isRepG0Decoders = new short[LZMACommon.NUM_STATES];
    private final short[] isRepG1Decoders = new short[LZMACommon.NUM_STATES];
    private final short[] isRepG2Decoders = new short[LZMACommon.NUM_STATES];
    private final short[] isRep0LongDecoders = new short[LZMACommon.NUM_STATES << LZMACommon.NUM_POS_STATES_BITS_MAX];
    private final BinTreeDecoder[] posSlotDecoder = new BinTreeDecoder[LZMACommon.NUM_LEN_TO_POS_STATES];
    private final short[] posDecoders = new short[LZMACommon.NUM_FULL_DISTANCES - LZMACommon.END_POS_MODEL_INDEX];
    private final BinTreeDecoder posAlignDecoder = new BinTreeDecoder(LZMACommon.NUM_ALIGN_BITS);
    private int dictionarySize = -1;
    private int dictionarySizeCheck = -1;
    private int posStateMask;
    private OutWindow outWindow;
    private LenDecoder lenDecoder;
    private LenDecoder repLenDecoder;
    private LiteralDecoder literalDecoder;

    public LZMADecoder(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        // setup LZ decoder
        for (int i = 0; i < LZMACommon.NUM_LEN_TO_POS_STATES; i++) {
            posSlotDecoder[i] = new BinTreeDecoder(LZMACommon.NUM_POS_SLOT_BITS);
        }

        byte[] properties = new byte[5];
        if (inputStream.read(properties) != 5) {
            throw new IOException("Failed to read LZMA header");
        }

        if (!setDecoderProperties(properties)) {
            throw new IOException("LZMADecoder properties cannot be set!");
        }

        long outSize = 0;
        for (int i = 0; i < 8; i++) {
            int v = inputStream.read();
            if (v < 0)
                throw new IOException("Can't read stream size");
            outSize |= ((long) v) << (8 * i);
        }

        this.outputSize = outSize;
    }

    private boolean setDictionarySize(int dictionarySize) {
        if (dictionarySize < 0) {
            return false;
        }
        if (this.dictionarySize != dictionarySize) {
            this.dictionarySize = dictionarySize;
            dictionarySizeCheck = Math.max(this.dictionarySize, 1);
            outWindow = new OutWindow(Math.max(dictionarySizeCheck, (1 << 12)));
        }
        return true;
    }

    private boolean setLcLpPb(int lc, int lp, int pb) {
        if (lc > LZMACommon.NUM_LIT_CONTEXT_BITS_MAX || lp > 4 || pb > LZMACommon.NUM_POS_STATES_BITS_MAX) {
            return false;
        }
        literalDecoder = new LiteralDecoder(lp, lc);
        int numPosStates = 1 << pb;
        lenDecoder = new LenDecoder(numPosStates);
        repLenDecoder = new LenDecoder(numPosStates);
        posStateMask = numPosStates - 1;
        return true;
    }

    private void initializeBitModels() throws IOException {
        RangeDecoder.initBitModels(isMatchDecoders);
        RangeDecoder.initBitModels(isRep0LongDecoders);
        RangeDecoder.initBitModels(isRepDecoders);
        RangeDecoder.initBitModels(isRepG0Decoders);
        RangeDecoder.initBitModels(isRepG1Decoders);
        RangeDecoder.initBitModels(isRepG2Decoders);
        RangeDecoder.initBitModels(posDecoders);
    }

    public void code() throws IOException {
        RangeDecoder rangeDecoder = new RangeDecoder(inputStream);
        outWindow.setStream(outputStream);
        initializeBitModels();

        int state = LZMACommon.INITIAL_STATE;
        int rep0 = 0, rep1 = 0, rep2 = 0, rep3 = 0;

        long nowPos64 = 0;
        byte prevByte = 0;
        while (outputSize < 0 || nowPos64 < outputSize) {
            int posState = (int) nowPos64 & posStateMask;
            if (rangeDecoder.decodeBit(isMatchDecoders, (state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState) == 0) {
                LiteralSubDecoder literalSubDecoder = literalDecoder.getDecoder((int) nowPos64, prevByte);
                if (!LZMACommon.stateIsCharState(state)) {
                    prevByte = literalSubDecoder.decodeWithMatchByte(rangeDecoder, outWindow.getByte(rep0));
                } else {
                    prevByte = literalSubDecoder.decodeNormal(rangeDecoder);
                }
                outWindow.putByte(prevByte);
                state = LZMACommon.stateUpdateChar(state);
                nowPos64++;
            } else {
                int len;
                if (rangeDecoder.decodeBit(isRepDecoders, state) == 1) {
                    len = 0;
                    if (rangeDecoder.decodeBit(isRepG0Decoders, state) == 0) {
                        if (rangeDecoder.decodeBit(isRep0LongDecoders, (state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState) == 0) {
                            state = LZMACommon.stateUpdateShortRep(state);
                            len = 1;
                        }
                    } else {
                        int distance;
                        if (rangeDecoder.decodeBit(isRepG1Decoders, state) == 0) {
                            distance = rep1;
                        } else {
                            if (rangeDecoder.decodeBit(isRepG2Decoders, state) == 0) {
                                distance = rep2;
                            } else {
                                distance = rep3;
                                rep3 = rep2;
                            }
                            rep2 = rep1;
                        }
                        rep1 = rep0;
                        rep0 = distance;
                    }
                    if (len == 0) {
                        len = repLenDecoder.decode(rangeDecoder, posState) + LZMACommon.MATCH_MIN_LEN;
                        state = LZMACommon.stateUpdateRep(state);
                    }
                } else {
                    rep3 = rep2;
                    rep2 = rep1;
                    rep1 = rep0;
                    len = LZMACommon.MATCH_MIN_LEN + lenDecoder.decode(rangeDecoder, posState);
                    state = LZMACommon.stateUpdateMatch(state);
                    int posSlot = posSlotDecoder[LZMACommon.getLenToPosState(len)].decode(rangeDecoder);
                    if (posSlot >= LZMACommon.START_POS_MODEL_INDEX) {
                        int numDirectBits = (posSlot >> 1) - 1;
                        rep0 = ((2 | (posSlot & 1)) << numDirectBits);
                        if (posSlot < LZMACommon.END_POS_MODEL_INDEX) {
                            rep0 += BinTreeDecoder.reverseDecode(posDecoders,
                                    rep0 - posSlot - 1, rangeDecoder, numDirectBits);
                        } else {
                            rep0 += (rangeDecoder.decodeDirectBits(
                                    numDirectBits - LZMACommon.NUM_ALIGN_BITS) << LZMACommon.NUM_ALIGN_BITS);
                            rep0 += posAlignDecoder.reverseDecode(rangeDecoder);
                            if (rep0 < 0) {
                                if (rep0 == -1) {
                                    break;
                                }
                                throw new IOException("Decoding failed!");
                            }
                        }
                    } else {
                        rep0 = posSlot;
                    }
                }
                if (rep0 >= nowPos64 || rep0 >= dictionarySizeCheck) {
                    // outWindow.flush();
                    throw new IOException("Decoding failed!");
                }
                outWindow.copyBlock(rep0, len);
                nowPos64 += len;
                prevByte = outWindow.getByte(0);
            }
        }
        outWindow.flush();
        outWindow.releaseStream();
    }

    private boolean setDecoderProperties(byte[] properties) {
        if (properties.length < 5) {
            return false;
        }

        int val = properties[0] & 0xFF;
        int lc = val % 9;
        int remainder = val / 9;
        int lp = remainder % 5;
        int pb = remainder / 5;
        int dictionarySize = 0;
        for (int i = 0; i < 4; i++) {
            dictionarySize += ((int) (properties[1 + i]) & 0xFF) << (i * 8);
        }
        return setLcLpPb(lc, lp, pb) && setDictionarySize(dictionarySize);
    }
}
