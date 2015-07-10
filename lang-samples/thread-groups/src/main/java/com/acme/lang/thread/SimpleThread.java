package com.acme.lang.thread;

public class SimpleThread extends Thread{

    @Override
    public void run (){
        synchronized ("A"){
            System.out.println (getName () + " about to wait.");
            try{
                "A".wait ();
            }catch (InterruptedException e){
                System.out.println (getName () + " interrupted.");
            }
            System.out.println (getName () + " terminating.");
        }
    }

}
