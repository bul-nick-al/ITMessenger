package com.example.nicholas.messengertest.CompressionAndConing.Compression.ShannonFano;
//package demmprogrammi1.ShannonCD;

import java.lang.Comparable;

class Pair implements Comparable<Pair> {
    private Byte MyByte;
    private Integer frequency;

    public Pair(Byte b, Integer a) {
        MyByte = b;
        frequency = a;
    }

    public Integer getFreq() {
        return frequency;
    }

    public Byte getByte() {
        return MyByte;
    }

    @Override
    public int compareTo(Pair other) {
        if (this.getFreq() > other.getFreq())
            return 1;
        if (other.getFreq() > this.getFreq())
            return -1;
        return 0;
    }
}