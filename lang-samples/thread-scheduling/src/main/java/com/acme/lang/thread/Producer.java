package com.acme.lang.thread;

import com.acme.lang.model.Share;

public class Producer extends Thread {
    private Share s;

    public Producer (Share s) {
        this.s = s;
    }

    @Override
    public void run (){
        for (char ch = 'A'; ch <= 'Z'; ch++){
            try{
                Thread.sleep ((int) (Math.random () * 4000));
            }catch (InterruptedException e) {}
            s.setSharedChar (ch);
            System.out.println (ch + " produced by producer.");
        }
    }
}
