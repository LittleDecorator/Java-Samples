package com.acme.lang;

public class YieldSample extends Thread {
    static boolean finished = false;
    static int sum = 0;

    public static void main (String [] args){
        new YieldSample ().start ();
        for (int i = 1; i <= 50000; i++){
            sum++;
            if (args.length == 0){
                Thread.yield ();
            }
        }
        finished = true;
    }

    @Override
    public void run (){
        while (!finished)
            System.out.println ("sum = " + sum);
    }
}
