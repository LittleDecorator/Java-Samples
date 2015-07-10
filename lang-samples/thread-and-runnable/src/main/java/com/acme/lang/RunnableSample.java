package com.acme.lang;

import java.applet.Applet;

public class RunnableSample extends Applet implements Runnable{

    private Thread t;

    @Override
    public void run (){
        while (t == Thread.currentThread ()){
            int width = rnd (30);
            if (width < 2){
                width += 2;
            }

            int height = rnd (10);
            if (height < 2){
                height += 2;
            }

            draw (width, height);
        }
    }

    @Override
    public void start (){
        if (t == null){
            t = new Thread (this);
            t.start ();
        }
    }

    @Override
    public void stop (){
        if (t != null){
            t = null;
        }
    }

    private void draw (int width, int height){
        for (int c = 0; c < width; c++){
            System.out.print ('*');
        }

        System.out.print ('\n');

        for (int r = 0; r < height - 2; r++){
            System.out.print ('*');

            for (int c = 0; c < width - 2; c++){
                System.out.print (' ');
            }

            System.out.print ('*');
            System.out.print ('\n');
        }

        for (int c = 0; c < width; c++){
            System.out.print ('*');
        }
        System.out.print ('\n');
    }

    private int rnd (int limit){
        // Return a random number x in the range 0 <= x < limit.
        return (int) (Math.random () * limit);
    }

}
