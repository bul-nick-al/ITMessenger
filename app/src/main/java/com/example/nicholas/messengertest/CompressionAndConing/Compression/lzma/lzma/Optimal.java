package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

class Optimal {
    int state;

    boolean prev1IsChar;
    boolean prev2;

    int price;

    int posPrev;
    int posPrev2;

    int backPrev;
    int backPrev2;

    int backs0;
    int backs1;
    int backs2;
    int backs3;

    void makeAsChar() {
        backPrev = -1;
        prev1IsChar = false;
    }

    void makeAsShortRep() {
        backPrev = 0;
        prev1IsChar = false;
    }

    boolean isShortRep() {
        return (backPrev == 0);
    }
}
