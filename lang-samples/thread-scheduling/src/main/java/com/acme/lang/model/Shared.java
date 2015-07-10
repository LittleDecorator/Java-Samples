package com.acme.lang.model;

public class Shared implements Share{

    private char c = '\u0000';
    public void setSharedChar (char c) { this.c = c; }
    public char getSharedChar () { return c; }

}
