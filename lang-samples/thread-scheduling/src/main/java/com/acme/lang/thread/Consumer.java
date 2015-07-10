package com.acme.lang.thread;

import com.acme.lang.model.Share;

public class Consumer extends Thread {
    private Share s;

    public Consumer (Share s){
        this.s = s;
    }

    @Override
    public void run (){
        char ch;
        do {
            try{
                Thread.sleep ((int) (Math.random () * 4000));
            } catch (InterruptedException e) {}
            ch = s.getSharedChar ();
            System.out.println (ch + " consumed by consumer.");
        }
        while (ch != 'Z');
    }
}
