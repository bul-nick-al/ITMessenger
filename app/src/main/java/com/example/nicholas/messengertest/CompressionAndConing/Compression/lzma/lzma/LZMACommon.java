package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

abstract class LZMACommon {
    static final int NUM_REP_DISTANCES = 4;
    static final int NUM_STATES = 12;
    static final int INITIAL_STATE = 0;
    static final int NUM_POS_SLOT_BITS = 6;
    static final int NUM_LEN_TO_POS_STATES_BITS = 2;
    static final int NUM_LEN_TO_POS_STATES = 1 << NUM_LEN_TO_POS_STATES_BITS;
    static final int MATCH_MIN_LEN = 2;
    static final int NUM_ALIGN_BITS = 4;
    static final int ALIGN_TABLE_SIZE = 1 << NUM_ALIGN_BITS;
    static final int ALIGN_MASK = (ALIGN_TABLE_SIZE - 1);
    static final int START_POS_MODEL_INDEX = 4;
    static final int END_POS_MODEL_INDEX = 14;
    static final int NUM_FULL_DISTANCES = 1 << (END_POS_MODEL_INDEX / 2);
    static final int NUM_LIT_CONTEXT_BITS_MAX = 8;
    static final int NUM_POS_STATES_BITS_MAX = 4;
    static final int NUM_POS_STATES_MAX = (1 << NUM_POS_STATES_BITS_MAX);
    static final int NUM_POS_STATES_BITS_ENCODING_MAX = 4;
    static final int NUM_POS_STATES_ENCODING_MAX = (1 << NUM_POS_STATES_BITS_ENCODING_MAX);
    static final int NUM_LOW_LEN_BITS = 3;
    static final int NUM_MID_LEN_BITS = 3;
    static final int NUM_HIGH_LEN_BITS = 8;
    static final int NUM_LOW_LEN_SYMBOLS = 1 << NUM_LOW_LEN_BITS;
    static final int NUM_MID_LEN_SYMBOLS = 1 << NUM_MID_LEN_BITS;
    static final int NUM_LEN_SYMBOLS = NUM_LOW_LEN_SYMBOLS + NUM_MID_LEN_SYMBOLS + (1 << NUM_HIGH_LEN_BITS);
    static final int MATCH_MAX_LEN = MATCH_MIN_LEN + NUM_LEN_SYMBOLS - 1;

    static int stateUpdateChar(int index) {
        if (index < 4) {
            return 0;
        }
        if (index < 10) {
            return index - 3;
        }
        return index - 6;
    }

    static int stateUpdateMatch(int index) {
        return (index < 7 ? 7 : 10);
    }

    static int stateUpdateRep(int index) {
        return (index < 7 ? 8 : 11);
    }

    static int stateUpdateShortRep(int index) {
        return (index < 7 ? 9 : 11);
    }

    static boolean stateIsCharState(int index) {
        return index < 7;
    }

    static int getLenToPosState(int len) {
        len -= MATCH_MIN_LEN;
        if (len < NUM_LEN_TO_POS_STATES) {
            return len;
        }
        return NUM_LEN_TO_POS_STATES - 1;
    }

}
