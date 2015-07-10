package com.acme.lang;

import com.acme.lang.thread.PrintThread;

public class NameThread {

    public static void main(String[] args) {
        PrintThread mt;
        if (args.length == 0) {
            mt = new PrintThread();
        } else {
            mt = new PrintThread(args[0]);
        }
        mt.start();
    }
}

