package com.acme.lang;

import com.acme.lang.thread.CalcThread;

public class JoinCalcPi {

    public static void main (String [] args){
        CalcThread mt = new CalcThread ();
        mt.start ();
        try{
            mt.join ();
        }catch (InterruptedException e){}
        System.out.println ("pi = " + mt.pi);
    }

}
