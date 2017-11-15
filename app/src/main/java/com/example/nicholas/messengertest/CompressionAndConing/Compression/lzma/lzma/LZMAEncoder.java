package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LZMAEncoder {
    private static final int DEFAULT_DICTIONARY_LOG_SIZE = 22;
    private static final int NUM_FAST_BYTES_DEFAULT = 0x20;
    private static final int NUM_OPTS = 1 << 12;
    private static final int INFINITY_PRICE = 0xFFFFFFF;
    private static final int FAST_SLOTS = 22;
    private static byte[] gFastPos = new byte[1 << 11];

    private int state = LZMACommon.INITIAL_STATE;
    private byte previousByte;
    private int[] repDistances = new int[LZMACommon.NUM_REP_DISTANCES];
    private Optimal[] optimum = new Optimal[NUM_OPTS];
    private BinTree matchFinder;
    private RangeEncoder rangeEncoder;
    private short[] isMatch = new short[LZMACommon.NUM_STATES << LZMACommon.NUM_POS_STATES_BITS_MAX];
    private short[] isRep = new short[LZMACommon.NUM_STATES];
    private short[] isRepG0 = new short[LZMACommon.NUM_STATES];
    private short[] isRepG1 = new short[LZMACommon.NUM_STATES];
    private short[] isRepG2 = new short[LZMACommon.NUM_STATES];
    private short[] isRep0Long = new short[LZMACommon.NUM_STATES << LZMACommon.NUM_POS_STATES_BITS_MAX];
    private BinTreeEncoder[] posSlotEncoder = new BinTreeEncoder[LZMACommon.NUM_LEN_TO_POS_STATES];
    private short[] posEncoders = new short[LZMACommon.NUM_FULL_DISTANCES - LZMACommon.END_POS_MODEL_INDEX];
    private BinTreeEncoder posAlignEncoder;
    private LenPriceTableEncoder lenEncoder;
    private LenPriceTableEncoder repMatchLenEncoder;
    private LiteralEncoder literalEncoder;
    private int[] matchDistances = new int[LZMACommon.MATCH_MAX_LEN * 2 + 2];
    private int numFastBytes = NUM_FAST_BYTES_DEFAULT;
    private int longestMatchLength;
    private int numDistancePairs;
    private int additionalOffset;
    private int optimumEndIndex;
    private int optimumCurrentIndex;
    private boolean longestMatchWasFound;
    private int[] posSlotPrices = new int[1 << (LZMACommon.NUM_POS_SLOT_BITS + LZMACommon.NUM_LEN_TO_POS_STATES_BITS)];
    private int[] distancesPrices = new int[LZMACommon.NUM_FULL_DISTANCES << LZMACommon.NUM_LEN_TO_POS_STATES_BITS];
    private int[] alignPrices = new int[LZMACommon.ALIGN_TABLE_SIZE];
    private int alignPriceCount;
    private int posStateBits = 2;
    private int posStateMask = (4 - 1);
    private int numLiteralPosStateBits = 0;
    private int numLiteralContextBits = 3;
    private int dictionarySize = (1 << DEFAULT_DICTIONARY_LOG_SIZE);
    private long nowPos64;
    private InputStream inputStream;
    private boolean finished = false;
    private int[] reps = new int[LZMACommon.NUM_REP_DISTANCES];
    private int[] repLens = new int[LZMACommon.NUM_REP_DISTANCES];
    private int backRes;
    private static final int PROP_SIZE = 5;
    private byte[] properties = new byte[PROP_SIZE];
    private int[] tempPrices = new int[LZMACommon.NUM_FULL_DISTANCES];
    private int matchPriceCount;

    private void precalculateFastPos() {
        int c = 2;
        gFastPos[0] = 0;
        gFastPos[1] = 1;
        for (int slotFast = 2; slotFast < FAST_SLOTS; slotFast++) {
            int k = (1 << ((slotFast >> 1) - 1));
            for (int j = 0; j < k; j++, c++) {
                gFastPos[c] = (byte) slotFast;
            }
        }
    }

    public LZMAEncoder(InputStream inputStream, OutputStream outputStream) throws IOException {
        precalculateFastPos();
        for (int i = 0; i < NUM_OPTS; i++) {
            optimum[i] = new Optimal();
        }

        for (int i = 0; i < LZMACommon.NUM_LEN_TO_POS_STATES; i++) {
            posSlotEncoder[i] = new BinTreeEncoder(LZMACommon.NUM_POS_SLOT_BITS);
        }

        // setInputStream()
        this.inputStream = inputStream;

        // setup encoders
        literalEncoder = new LiteralEncoder(numLiteralPosStateBits, numLiteralContextBits);
        matchFinder = new BinTree(inputStream, 4, dictionarySize, NUM_OPTS, numFastBytes, LZMACommon.MATCH_MAX_LEN + 1);
        rangeEncoder = new RangeEncoder(outputStream);

        // initializeDecoders()
        state = LZMACommon.INITIAL_STATE;
        previousByte = 0;
        for (int i = 0; i < LZMACommon.NUM_REP_DISTANCES; i++) {
            repDistances[i] = 0;
        }

        RangeEncoder.initBitModels(isMatch);
        RangeEncoder.initBitModels(isRep0Long);
        RangeEncoder.initBitModels(isRep);
        RangeEncoder.initBitModels(isRepG0);
        RangeEncoder.initBitModels(isRepG1);
        RangeEncoder.initBitModels(isRepG2);
        RangeEncoder.initBitModels(posEncoders);

        literalEncoder.init();
        /*
        for (int i = 0; i < LZMACommon.NUM_LEN_TO_POS_STATES; i++) {
            posSlotEncoder[i].init();
        }
        */

        lenEncoder = new LenPriceTableEncoder(1 << posStateBits);
        repMatchLenEncoder = new LenPriceTableEncoder(1 << posStateBits);

        posAlignEncoder = new BinTreeEncoder(LZMACommon.NUM_ALIGN_BITS);

        longestMatchWasFound = false;
        optimumEndIndex = 0;
        optimumCurrentIndex = 0;
        additionalOffset = 0;

        fillDistancesPrices();
        fillAlignPrices();

        lenEncoder.setTableSize(numFastBytes + 1 - LZMACommon.MATCH_MIN_LEN);
        lenEncoder.updateTables(1 << posStateBits);
        repMatchLenEncoder.setTableSize(numFastBytes + 1 - LZMACommon.MATCH_MIN_LEN);
        repMatchLenEncoder.updateTables(1 << posStateBits);
        nowPos64 = 0;

        writeCoderProperties(outputStream);

        for (int i = 0; i < 8; i++) {
            outputStream.write((byte) -1);
        }
    }

    private static int getPosSlot(int pos) {
        if (pos < (1 << 11)) {
            return gFastPos[pos];
        }
        if (pos < (1 << 21)) {
            return (gFastPos[pos >> 10] + 20);
        }
        return (gFastPos[pos >> 20] + 40);
    }

    private static int getPosSlot2(int pos) {
        if (pos < (1 << 17)) {
            return (gFastPos[pos >> 6] + 12);
        }
        if (pos < (1 << 27)) {
            return (gFastPos[pos >> 16] + 32);
        }
        return (gFastPos[pos >> 26] + 52);
    }

    private int readMatchDistances() throws IOException {
        int lenRes = 0;
        numDistancePairs = matchFinder.getMatches(matchDistances);
        if (numDistancePairs > 0) {
            lenRes = matchDistances[numDistancePairs - 2];
            if (lenRes == numFastBytes) {
                lenRes += matchFinder.getMatchLen(lenRes - 1, matchDistances[numDistancePairs - 1],
                        LZMACommon.MATCH_MAX_LEN - lenRes);
            }
        }
        additionalOffset++;
        return lenRes;
    }

    private void movePos(int num) throws IOException {
        if (num > 0) {
            matchFinder.skip(num);
            additionalOffset += num;
        }
    }

    private int getRepLen1Price(int state, int posState) {
        return RangeEncoder.getPrice0(isRepG0[state]) +
                RangeEncoder.getPrice0(isRep0Long[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]);
    }

    private int getPureRepPrice(int repIndex, int state, int posState) {
        int price;
        if (repIndex == 0) {
            price = RangeEncoder.getPrice0(isRepG0[state]);
            price += RangeEncoder.getPrice1(isRep0Long[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]);
        } else {
            price = RangeEncoder.getPrice1(isRepG0[state]);
            if (repIndex == 1) {
                price += RangeEncoder.getPrice0(isRepG1[state]);
            } else {
                price += RangeEncoder.getPrice1(isRepG1[state]);
                price += RangeEncoder.getPrice(isRepG2[state], repIndex - 2);
            }
        }
        return price;
    }

    private int getRepPrice(int repIndex, int len, int state, int posState) {
        int price = repMatchLenEncoder.getPrice(len - LZMACommon.MATCH_MIN_LEN, posState);
        return price + getPureRepPrice(repIndex, state, posState);
    }

    private int getPosLenPrice(int pos, int len, int posState) {
        int price;
        int lenToPosState = LZMACommon.getLenToPosState(len);
        if (pos < LZMACommon.NUM_FULL_DISTANCES) {
            price = distancesPrices[(lenToPosState * LZMACommon.NUM_FULL_DISTANCES) + pos];
        } else {
            price = posSlotPrices[(lenToPosState << LZMACommon.NUM_POS_SLOT_BITS) + getPosSlot2(pos)] +
                    alignPrices[pos & LZMACommon.ALIGN_MASK];
        }
        return price + lenEncoder.getPrice(len - LZMACommon.MATCH_MIN_LEN, posState);
    }

    private int backward(int current) {
        optimumEndIndex = current;
        int posMem = optimum[current].posPrev;
        int backMem = optimum[current].backPrev;
        do {
            if (optimum[current].prev1IsChar) {
                optimum[posMem].makeAsChar();
                optimum[posMem].posPrev = posMem - 1;
                if (optimum[current].prev2) {
                    optimum[posMem - 1].prev1IsChar = false;
                    optimum[posMem - 1].posPrev = optimum[current].posPrev2;
                    optimum[posMem - 1].backPrev = optimum[current].backPrev2;
                }
            }
            int posPrev = posMem;
            int backCur = backMem;

            backMem = optimum[posPrev].backPrev;
            posMem = optimum[posPrev].posPrev;

            optimum[posPrev].backPrev = backCur;
            optimum[posPrev].posPrev = current;
            current = posPrev;
        }
        while (current > 0);
        backRes = optimum[0].backPrev;
        optimumCurrentIndex = optimum[0].posPrev;
        return optimumCurrentIndex;
    }

    private int getOptimum(int position) throws IOException {
        if (optimumEndIndex != optimumCurrentIndex) {
            int lenRes = optimum[optimumCurrentIndex].posPrev - optimumCurrentIndex;
            backRes = optimum[optimumCurrentIndex].backPrev;
            optimumCurrentIndex = optimum[optimumCurrentIndex].posPrev;
            return lenRes;
        }
        optimumCurrentIndex = optimumEndIndex = 0;

        int lenMain, numDistancePairs;
        if (!longestMatchWasFound) {
            lenMain = readMatchDistances();
        } else {
            lenMain = longestMatchLength;
            longestMatchWasFound = false;
        }
        numDistancePairs = this.numDistancePairs;

        int numAvailableBytes = matchFinder.getNumAvailableBytes() + 1;
        if (numAvailableBytes < 2) {
            backRes = -1;
            return 1;
        }

        int repMaxIndex = 0;
        int i;
        for (i = 0; i < LZMACommon.NUM_REP_DISTANCES; i++) {
            reps[i] = repDistances[i];
            repLens[i] = matchFinder.getMatchLen(0 - 1, reps[i], LZMACommon.MATCH_MAX_LEN);
            if (repLens[i] > repLens[repMaxIndex]) {
                repMaxIndex = i;
            }
        }
        if (repLens[repMaxIndex] >= numFastBytes) {
            backRes = repMaxIndex;
            int lenRes = repLens[repMaxIndex];
            movePos(lenRes - 1);
            return lenRes;
        }

        if (lenMain >= numFastBytes) {
            backRes = matchDistances[numDistancePairs - 1] + LZMACommon.NUM_REP_DISTANCES;
            movePos(lenMain - 1);
            return lenMain;
        }

        byte currentByte = matchFinder.getIndexByte(0 - 1);
        byte matchByte = matchFinder.getIndexByte(0 - repDistances[0] - 1 - 1);

        if (lenMain < 2 && currentByte != matchByte && repLens[repMaxIndex] < 2) {
            backRes = -1;
            return 1;
        }

        optimum[0].state = state;

        int posState = (position & posStateMask);

        optimum[1].price = RangeEncoder.getPrice0(isMatch[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]) +
                literalEncoder.getSubCoder(position, previousByte).getPrice(!LZMACommon.stateIsCharState(state), matchByte, currentByte);
        optimum[1].makeAsChar();

        int matchPrice = RangeEncoder.getPrice1(isMatch[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]);
        int repMatchPrice = matchPrice + RangeEncoder.getPrice1(isRep[state]);

        if (matchByte == currentByte) {
            int shortRepPrice = repMatchPrice + getRepLen1Price(state, posState);
            if (shortRepPrice < optimum[1].price) {
                optimum[1].price = shortRepPrice;
                optimum[1].makeAsShortRep();
            }
        }

        int lenEnd = ((lenMain >= repLens[repMaxIndex]) ? lenMain : repLens[repMaxIndex]);

        if (lenEnd < 2) {
            backRes = optimum[1].backPrev;
            return 1;
        }

        optimum[1].posPrev = 0;

        optimum[0].backs0 = reps[0];
        optimum[0].backs1 = reps[1];
        optimum[0].backs2 = reps[2];
        optimum[0].backs3 = reps[3];

        int len = lenEnd;
        do {
            optimum[len--].price = INFINITY_PRICE;
        }
        while (len >= 2);

        for (i = 0; i < LZMACommon.NUM_REP_DISTANCES; i++) {
            int repLen = repLens[i];
            if (repLen < 2) {
                continue;
            }
            int price = repMatchPrice + getPureRepPrice(i, state, posState);
            do {
                int curAndLenPrice = price + repMatchLenEncoder.getPrice(repLen - 2, posState);
                Optimal optimum = this.optimum[repLen];
                if (curAndLenPrice < optimum.price) {
                    optimum.price = curAndLenPrice;
                    optimum.posPrev = 0;
                    optimum.backPrev = i;
                    optimum.prev1IsChar = false;
                }
            }
            while (--repLen >= 2);
        }

        int normalMatchPrice = matchPrice + RangeEncoder.getPrice0(isRep[state]);

        len = ((repLens[0] >= 2) ? repLens[0] + 1 : 2);
        if (len <= lenMain) {
            int offs = 0;
            while (len > matchDistances[offs]) {
                offs += 2;
            }
            for (; ; len++) {
                int distance = matchDistances[offs + 1];
                int curAndLenPrice = normalMatchPrice + getPosLenPrice(distance, len, posState);
                Optimal optimum = this.optimum[len];
                if (curAndLenPrice < optimum.price) {
                    optimum.price = curAndLenPrice;
                    optimum.posPrev = 0;
                    optimum.backPrev = distance + LZMACommon.NUM_REP_DISTANCES;
                    optimum.prev1IsChar = false;
                }
                if (len == matchDistances[offs]) {
                    offs += 2;
                    if (offs == numDistancePairs) {
                        break;
                    }
                }
            }
        }

        int cur = 0;

        while (true) {
            cur++;
            if (cur == lenEnd) {
                return backward(cur);
            }
            int newLen = readMatchDistances();
            numDistancePairs = this.numDistancePairs;
            if (newLen >= numFastBytes) {

                longestMatchLength = newLen;
                longestMatchWasFound = true;
                return backward(cur);
            }
            position++;
            int posPrev = optimum[cur].posPrev;
            int state;
            if (optimum[cur].prev1IsChar) {
                posPrev--;
                if (optimum[cur].prev2) {
                    state = optimum[optimum[cur].posPrev2].state;
                    if (optimum[cur].backPrev2 < LZMACommon.NUM_REP_DISTANCES) {
                        state = LZMACommon.stateUpdateRep(state);
                    } else {
                        state = LZMACommon.stateUpdateMatch(state);
                    }
                } else {
                    state = optimum[posPrev].state;
                }
                state = LZMACommon.stateUpdateChar(state);
            } else {
                state = optimum[posPrev].state;
            }
            if (posPrev == cur - 1) {
                if (optimum[cur].isShortRep()) {
                    state = LZMACommon.stateUpdateShortRep(state);
                } else {
                    state = LZMACommon.stateUpdateChar(state);
                }
            } else {
                int pos;
                if (optimum[cur].prev1IsChar && optimum[cur].prev2) {
                    posPrev = optimum[cur].posPrev2;
                    pos = optimum[cur].backPrev2;
                    state = LZMACommon.stateUpdateRep(state);
                } else {
                    pos = optimum[cur].backPrev;
                    if (pos < LZMACommon.NUM_REP_DISTANCES) {
                        state = LZMACommon.stateUpdateRep(state);
                    } else {
                        state = LZMACommon.stateUpdateMatch(state);
                    }
                }
                Optimal opt = optimum[posPrev];
                if (pos < LZMACommon.NUM_REP_DISTANCES) {
                    if (pos == 0) {
                        reps[0] = opt.backs0;
                        reps[1] = opt.backs1;
                        reps[2] = opt.backs2;
                        reps[3] = opt.backs3;
                    } else if (pos == 1) {
                        reps[0] = opt.backs1;
                        reps[1] = opt.backs0;
                        reps[2] = opt.backs2;
                        reps[3] = opt.backs3;
                    } else if (pos == 2) {
                        reps[0] = opt.backs2;
                        reps[1] = opt.backs0;
                        reps[2] = opt.backs1;
                        reps[3] = opt.backs3;
                    } else {
                        reps[0] = opt.backs3;
                        reps[1] = opt.backs0;
                        reps[2] = opt.backs1;
                        reps[3] = opt.backs2;
                    }
                } else {
                    reps[0] = (pos - LZMACommon.NUM_REP_DISTANCES);
                    reps[1] = opt.backs0;
                    reps[2] = opt.backs1;
                    reps[3] = opt.backs2;
                }
            }
            optimum[cur].state = state;
            optimum[cur].backs0 = reps[0];
            optimum[cur].backs1 = reps[1];
            optimum[cur].backs2 = reps[2];
            optimum[cur].backs3 = reps[3];
            int curPrice = optimum[cur].price;

            currentByte = matchFinder.getIndexByte(0 - 1);
            matchByte = matchFinder.getIndexByte(0 - reps[0] - 1 - 1);

            posState = (position & posStateMask);

            int curAnd1Price = curPrice +
                    RangeEncoder.getPrice0(isMatch[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]) +
                    literalEncoder.getSubCoder(position, matchFinder.getIndexByte(0 - 2)).
                            getPrice(!LZMACommon.stateIsCharState(state), matchByte, currentByte);

            Optimal nextOptimum = optimum[cur + 1];

            boolean nextIsChar = false;
            if (curAnd1Price < nextOptimum.price) {
                nextOptimum.price = curAnd1Price;
                nextOptimum.posPrev = cur;
                nextOptimum.makeAsChar();
                nextIsChar = true;
            }

            matchPrice = curPrice + RangeEncoder.getPrice1(isMatch[(state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState]);
            repMatchPrice = matchPrice + RangeEncoder.getPrice1(isRep[state]);

            if (matchByte == currentByte &&
                    !(nextOptimum.posPrev < cur && nextOptimum.backPrev == 0)) {
                int shortRepPrice = repMatchPrice + getRepLen1Price(state, posState);
                if (shortRepPrice <= nextOptimum.price) {
                    nextOptimum.price = shortRepPrice;
                    nextOptimum.posPrev = cur;
                    nextOptimum.makeAsShortRep();
                    nextIsChar = true;
                }
            }

            int numAvailableBytesFull = matchFinder.getNumAvailableBytes() + 1;
            numAvailableBytesFull = Math.min(NUM_OPTS - 1 - cur, numAvailableBytesFull);
            numAvailableBytes = numAvailableBytesFull;

            if (numAvailableBytes < 2) {
                continue;
            }
            if (numAvailableBytes > numFastBytes) {
                numAvailableBytes = numFastBytes;
            }
            if (!nextIsChar && matchByte != currentByte) {
                // try Literal + rep0
                int t = Math.min(numAvailableBytesFull - 1, numFastBytes);
                int lenTest2 = matchFinder.getMatchLen(0, reps[0], t);
                if (lenTest2 >= 2) {
                    int state2 = LZMACommon.stateUpdateChar(state);

                    int posStateNext = (position + 1) & posStateMask;
                    int nextRepMatchPrice = curAnd1Price +
                            RangeEncoder.getPrice1(isMatch[(state2 << LZMACommon.NUM_POS_STATES_BITS_MAX) + posStateNext]) +
                            RangeEncoder.getPrice1(isRep[state2]);
                    {
                        int offset = cur + 1 + lenTest2;
                        while (lenEnd < offset) {
                            optimum[++lenEnd].price = INFINITY_PRICE;
                        }
                        int curAndLenPrice = nextRepMatchPrice + getRepPrice(
                                0, lenTest2, state2, posStateNext);
                        Optimal optimum = this.optimum[offset];
                        if (curAndLenPrice < optimum.price) {
                            optimum.price = curAndLenPrice;
                            optimum.posPrev = cur + 1;
                            optimum.backPrev = 0;
                            optimum.prev1IsChar = true;
                            optimum.prev2 = false;
                        }
                    }
                }
            }

            int startLen = 2; // speed optimization

            for (int repIndex = 0; repIndex < LZMACommon.NUM_REP_DISTANCES; repIndex++) {
                int lenTest = matchFinder.getMatchLen(0 - 1, reps[repIndex], numAvailableBytes);
                if (lenTest < 2) {
                    continue;
                }
                int lenTestTemp = lenTest;
                do {
                    while (lenEnd < cur + lenTest) {
                        optimum[++lenEnd].price = INFINITY_PRICE;
                    }
                    int curAndLenPrice = repMatchPrice + getRepPrice(repIndex, lenTest, state, posState);
                    Optimal optimum = this.optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.price) {
                        optimum.price = curAndLenPrice;
                        optimum.posPrev = cur;
                        optimum.backPrev = repIndex;
                        optimum.prev1IsChar = false;
                    }
                }
                while (--lenTest >= 2);
                lenTest = lenTestTemp;

                if (repIndex == 0) {
                    startLen = lenTest + 1;
                }

                // if (_maxMode)
                if (lenTest < numAvailableBytesFull) {
                    int t = Math.min(numAvailableBytesFull - 1 - lenTest, numFastBytes);
                    int lenTest2 = matchFinder.getMatchLen(lenTest, reps[repIndex], t);
                    if (lenTest2 >= 2) {
                        int state2 = LZMACommon.stateUpdateRep(state);

                        int posStateNext = (position + lenTest) & posStateMask;
                        int curAndLenCharPrice =
                                repMatchPrice + getRepPrice(repIndex, lenTest, state, posState) +
                                        RangeEncoder.getPrice0(isMatch[(state2 << LZMACommon.NUM_POS_STATES_BITS_MAX) + posStateNext]) +
                                        literalEncoder.getSubCoder(position + lenTest,
                                                matchFinder.getIndexByte(lenTest - 1 - 1)).getPrice(true,
                                                matchFinder.getIndexByte(lenTest - 1 - (reps[repIndex] + 1)),
                                                matchFinder.getIndexByte(lenTest - 1));
                        state2 = LZMACommon.stateUpdateChar(state2);
                        posStateNext = (position + lenTest + 1) & posStateMask;
                        int nextMatchPrice = curAndLenCharPrice + RangeEncoder.getPrice1(isMatch[(state2 << LZMACommon.NUM_POS_STATES_BITS_MAX) + posStateNext]);
                        int nextRepMatchPrice = nextMatchPrice + RangeEncoder.getPrice1(isRep[state2]);

                        // for(; lenTest2 >= 2; lenTest2--)
                        {
                            int offset = lenTest + 1 + lenTest2;
                            while (lenEnd < cur + offset) {
                                optimum[++lenEnd].price = INFINITY_PRICE;
                            }
                            int curAndLenPrice = nextRepMatchPrice + getRepPrice(0, lenTest2, state2, posStateNext);
                            Optimal optimum = this.optimum[cur + offset];
                            if (curAndLenPrice < optimum.price) {
                                optimum.price = curAndLenPrice;
                                optimum.posPrev = cur + lenTest + 1;
                                optimum.backPrev = 0;
                                optimum.prev1IsChar = true;
                                optimum.prev2 = true;
                                optimum.posPrev2 = cur;
                                optimum.backPrev2 = repIndex;
                            }
                        }
                    }
                }
            }

            if (newLen > numAvailableBytes) {
                newLen = numAvailableBytes;
                numDistancePairs = 0;
                while (newLen > matchDistances[numDistancePairs]) {
                    numDistancePairs += 2;
                }
                matchDistances[numDistancePairs] = newLen;
                numDistancePairs += 2;
            }
            if (newLen >= startLen) {
                normalMatchPrice = matchPrice + RangeEncoder.getPrice0(isRep[state]);
                while (lenEnd < cur + newLen) {
                    optimum[++lenEnd].price = INFINITY_PRICE;
                }

                int offs = 0;
                while (startLen > matchDistances[offs]) {
                    offs += 2;
                }

                for (int lenTest = startLen; ; lenTest++) {
                    int curBack = matchDistances[offs + 1];
                    int curAndLenPrice = normalMatchPrice + getPosLenPrice(curBack, lenTest, posState);
                    Optimal optimum = this.optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.price) {
                        optimum.price = curAndLenPrice;
                        optimum.posPrev = cur;
                        optimum.backPrev = curBack + LZMACommon.NUM_REP_DISTANCES;
                        optimum.prev1IsChar = false;
                    }

                    if (lenTest == matchDistances[offs]) {
                        if (lenTest < numAvailableBytesFull) {
                            int t = Math.min(numAvailableBytesFull - 1 - lenTest, numFastBytes);
                            int lenTest2 = matchFinder.getMatchLen(lenTest, curBack, t);
                            if (lenTest2 >= 2) {
                                int state2 = LZMACommon.stateUpdateMatch(state);

                                int posStateNext = (position + lenTest) & posStateMask;
                                int curAndLenCharPrice = curAndLenPrice +
                                        RangeEncoder.getPrice0(isMatch[(state2 << LZMACommon.NUM_POS_STATES_BITS_MAX) + posStateNext]) +
                                        literalEncoder.getSubCoder(position + lenTest,
                                                matchFinder.getIndexByte(lenTest - 1 - 1)).
                                                getPrice(true,
                                                        matchFinder.getIndexByte(lenTest - (curBack + 1) - 1),
                                                        matchFinder.getIndexByte(lenTest - 1));
                                state2 = LZMACommon.stateUpdateChar(state2);
                                posStateNext = (position + lenTest + 1) & posStateMask;
                                int nextMatchPrice = curAndLenCharPrice + RangeEncoder.getPrice1(isMatch[(state2 << LZMACommon.NUM_POS_STATES_BITS_MAX) + posStateNext]);
                                int nextRepMatchPrice = nextMatchPrice + RangeEncoder.getPrice1(isRep[state2]);

                                int offset = lenTest + 1 + lenTest2;
                                while (lenEnd < cur + offset) {
                                    this.optimum[++lenEnd].price = INFINITY_PRICE;
                                }
                                curAndLenPrice = nextRepMatchPrice + getRepPrice(0, lenTest2, state2, posStateNext);
                                optimum = this.optimum[cur + offset];
                                if (curAndLenPrice < optimum.price) {
                                    optimum.price = curAndLenPrice;
                                    optimum.posPrev = cur + lenTest + 1;
                                    optimum.backPrev = 0;
                                    optimum.prev1IsChar = true;
                                    optimum.prev2 = true;
                                    optimum.posPrev2 = cur;
                                    optimum.backPrev2 = curBack + LZMACommon.NUM_REP_DISTANCES;
                                }
                            }
                        }
                        offs += 2;
                        if (offs == numDistancePairs) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void writeEndMarker(int posState) throws IOException {
        rangeEncoder.encode(isMatch, (state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState, 1);
        rangeEncoder.encode(isRep, state, 0);
        state = LZMACommon.stateUpdateMatch(state);
        int len = LZMACommon.MATCH_MIN_LEN;
        lenEncoder.encode(rangeEncoder, len - LZMACommon.MATCH_MIN_LEN, posState);
        int posSlot = (1 << LZMACommon.NUM_POS_SLOT_BITS) - 1;
        int lenToPosState = LZMACommon.getLenToPosState(len);
        posSlotEncoder[lenToPosState].encode(rangeEncoder, posSlot);
        int footerBits = 30;
        int posReduced = (1 << footerBits) - 1;
        rangeEncoder.encodeDirectBits(posReduced >> LZMACommon.NUM_ALIGN_BITS, footerBits - LZMACommon.NUM_ALIGN_BITS);
        posAlignEncoder.reverseEncode(rangeEncoder, posReduced & LZMACommon.ALIGN_MASK);
    }

    private void flush(int nowPos) throws IOException {
        writeEndMarker(nowPos & posStateMask);
        rangeEncoder.flush();
    }

    private void codeOneBlock() throws IOException {
        finished = true;

        if (inputStream != null) {
            matchFinder.init();
            inputStream = null;
        }

        long progressPosValuePrev = nowPos64;
        if (nowPos64 == 0) {
            if (matchFinder.getNumAvailableBytes() == 0) {
                flush((int) nowPos64);
                return;
            }

            readMatchDistances();
            int posState = (int) (nowPos64) & posStateMask;
            rangeEncoder.encode(isMatch, (state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState, 0);
            state = LZMACommon.stateUpdateChar(state);
            byte curByte = matchFinder.getIndexByte(0 - additionalOffset);
            literalEncoder.getSubCoder((int) (nowPos64), previousByte).encode(rangeEncoder, curByte);
            previousByte = curByte;
            additionalOffset--;
            nowPos64++;
        }
        if (matchFinder.getNumAvailableBytes() == 0) {
            flush((int) nowPos64);
            return;
        }
        while (true) {

            int len = getOptimum((int) nowPos64);
            int pos = backRes;
            int posState = ((int) nowPos64) & posStateMask;
            int complexState = (state << LZMACommon.NUM_POS_STATES_BITS_MAX) + posState;
            if (len == 1 && pos == -1) {
                rangeEncoder.encode(isMatch, complexState, 0);
                byte curByte = matchFinder.getIndexByte(0 - additionalOffset);
                LiteralSubEncoder subCoder = literalEncoder.getSubCoder((int) nowPos64, previousByte);
                if (!LZMACommon.stateIsCharState(state)) {
                    byte matchByte = matchFinder.getIndexByte(0 - repDistances[0] - 1 - additionalOffset);
                    subCoder.encodeMatched(rangeEncoder, matchByte, curByte);
                } else {
                    subCoder.encode(rangeEncoder, curByte);
                }
                previousByte = curByte;
                state = LZMACommon.stateUpdateChar(state);
            } else {
                rangeEncoder.encode(isMatch, complexState, 1);
                if (pos < LZMACommon.NUM_REP_DISTANCES) {
                    rangeEncoder.encode(isRep, state, 1);
                    if (pos == 0) {
                        rangeEncoder.encode(isRepG0, state, 0);
                        if (len == 1) {
                            rangeEncoder.encode(isRep0Long, complexState, 0);
                        } else {
                            rangeEncoder.encode(isRep0Long, complexState, 1);
                        }
                    } else {
                        rangeEncoder.encode(isRepG0, state, 1);
                        if (pos == 1) {
                            rangeEncoder.encode(isRepG1, state, 0);
                        } else {
                            rangeEncoder.encode(isRepG1, state, 1);
                            rangeEncoder.encode(isRepG2, state, pos - 2);
                        }
                    }
                    if (len == 1) {
                        state = LZMACommon.stateUpdateShortRep(state);
                    } else {
                        repMatchLenEncoder.encode(rangeEncoder, len - LZMACommon.MATCH_MIN_LEN, posState);
                        state = LZMACommon.stateUpdateRep(state);
                    }
                    int distance = repDistances[pos];
                    if (pos != 0) {
                        System.arraycopy(repDistances, 0, repDistances, 1, pos);
                        repDistances[0] = distance;
                    }
                } else {
                    rangeEncoder.encode(isRep, state, 0);
                    state = LZMACommon.stateUpdateMatch(state);
                    lenEncoder.encode(rangeEncoder, len - LZMACommon.MATCH_MIN_LEN, posState);
                    pos -= LZMACommon.NUM_REP_DISTANCES;
                    int posSlot = getPosSlot(pos);
                    int lenToPosState = LZMACommon.getLenToPosState(len);
                    posSlotEncoder[lenToPosState].encode(rangeEncoder, posSlot);

                    if (posSlot >= LZMACommon.START_POS_MODEL_INDEX) {
                        int footerBits = (posSlot >> 1) - 1;
                        int baseVal = ((2 | (posSlot & 1)) << footerBits);
                        int posReduced = pos - baseVal;

                        if (posSlot < LZMACommon.END_POS_MODEL_INDEX) {
                            BinTreeEncoder.reverseEncode(posEncoders,
                                    baseVal - posSlot - 1, rangeEncoder, footerBits, posReduced);
                        } else {
                            rangeEncoder.encodeDirectBits(posReduced >> LZMACommon.NUM_ALIGN_BITS, footerBits - LZMACommon.NUM_ALIGN_BITS);
                            posAlignEncoder.reverseEncode(rangeEncoder, posReduced & LZMACommon.ALIGN_MASK);
                            alignPriceCount++;
                        }
                    }
                    int distance = pos;
                    System.arraycopy(repDistances, 0, repDistances, 1, LZMACommon.NUM_REP_DISTANCES - 1);
                    repDistances[0] = distance;
                    matchPriceCount++;
                }
                previousByte = matchFinder.getIndexByte(len - 1 - additionalOffset);
            }
            additionalOffset -= len;
            nowPos64 += len;
            if (additionalOffset == 0) {
                // if (!_fastMode)
                if (matchPriceCount >= (1 << 7)) {
                    fillDistancesPrices();
                }
                if (alignPriceCount >= LZMACommon.ALIGN_TABLE_SIZE) {
                    fillAlignPrices();
                }
                if (matchFinder.getNumAvailableBytes() == 0) {
                    flush((int) nowPos64);
                    return;
                }

                if (nowPos64 - progressPosValuePrev >= (1 << 12)) {
                    finished = false;
                    return;
                }
            }
        }
    }

    public void code() throws IOException {
        while (!finished) {
            codeOneBlock();
        }
    }

    public void writeCoderProperties(OutputStream outStream) throws IOException {
        properties[0] = (byte) ((posStateBits * 5 + numLiteralPosStateBits) * 9 + numLiteralContextBits);
        for (int i = 0; i < 4; i++) {
            properties[1 + i] = (byte) (dictionarySize >> (8 * i));
        }
        outStream.write(properties, 0, PROP_SIZE);
    }

    private void fillDistancesPrices() {
        for (int i = LZMACommon.START_POS_MODEL_INDEX; i < LZMACommon.NUM_FULL_DISTANCES; i++) {
            int posSlot = getPosSlot(i);
            int footerBits = (posSlot >> 1) - 1;
            int baseVal = ((2 | (posSlot & 1)) << footerBits);
            tempPrices[i] = BinTreeEncoder.reverseGetPrice(posEncoders,
                    baseVal - posSlot - 1, footerBits, i - baseVal);
        }

        for (int lenToPosState = 0; lenToPosState < LZMACommon.NUM_LEN_TO_POS_STATES; lenToPosState++) {
            int posSlot;
            BinTreeEncoder encoder = posSlotEncoder[lenToPosState];

            int st = (lenToPosState << LZMACommon.NUM_POS_SLOT_BITS);
            int distTableSize = (DEFAULT_DICTIONARY_LOG_SIZE * 2);
            for (posSlot = 0; posSlot < distTableSize; posSlot++) {
                posSlotPrices[st + posSlot] = encoder.getPrice(posSlot);
            }
            for (posSlot = LZMACommon.END_POS_MODEL_INDEX; posSlot < distTableSize; posSlot++) {
                posSlotPrices[st + posSlot] += ((((posSlot >> 1) - 1) - LZMACommon.NUM_ALIGN_BITS) << RangeEncoder.NUM_BIT_PRICE_SHIFT_BITS);
            }

            int st2 = lenToPosState * LZMACommon.NUM_FULL_DISTANCES;
            int i;
            for (i = 0; i < LZMACommon.START_POS_MODEL_INDEX; i++) {
                distancesPrices[st2 + i] = posSlotPrices[st + i];
            }
            for (; i < LZMACommon.NUM_FULL_DISTANCES; i++) {
                distancesPrices[st2 + i] = posSlotPrices[st + getPosSlot(i)] + tempPrices[i];
            }
        }
        matchPriceCount = 0;
    }

    private void fillAlignPrices() {
        for (int i = 0; i < LZMACommon.ALIGN_TABLE_SIZE; i++) {
            alignPrices[i] = posAlignEncoder.reverseGetPrice(i);
        }
        alignPriceCount = 0;
    }
}

