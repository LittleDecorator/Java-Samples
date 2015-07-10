package com.acme.lang;

public class ThreadLocalSample {

    public static void main (String [] args)
    {
        InitLocalThread mt1 = new InitLocalThread ("A");
        InitLocalThread mt2 = new InitLocalThread ("B");
        InitLocalThread mt3 = new InitLocalThread ("C");
        mt1.start ();
        mt2.start ();
        mt3.start ();
    }
}

class InitLocalThread extends Thread {

    //it works because sernum is class variable, not instance.
    private static ThreadLocal tl = new
            ThreadLocal (){
                @Override
                protected synchronized Object initialValue (){
                    return new Integer (sernum++);
                }
            };

    private static int sernum = 100;

    InitLocalThread (String name){
        super (name);
    }

    public void run (){
        for (int i = 0; i < 10; i++)
            System.out.println (getName () + " " + tl.get ());
    }
}