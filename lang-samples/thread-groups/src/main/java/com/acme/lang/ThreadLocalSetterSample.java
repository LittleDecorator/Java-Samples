package com.acme.lang;

public class ThreadLocalSetterSample {

    public static void main (String [] args){
        SetterThread mt1 = new SetterThread ("A");
        SetterThread mt2 = new SetterThread ("B");
        SetterThread mt3 = new SetterThread ("C");
        mt1.start ();
        mt2.start ();
        mt3.start ();
    }
}

class SetterThread extends Thread {
    private static ThreadLocal tl = new ThreadLocal ();
    private static int sernum = 100;

    SetterThread (String name){
        super (name);
    }

    @Override
    public void run (){
        //it works because String object is the same for all threads. Thanks for cache.
        synchronized ("A"){
            tl.set ("" + sernum++);
        }
        for (int i = 0; i < 10; i++)
            System.out.println (getName () + " " + tl.get ());
    }
}
