package com.acme.lang.thread;

public class CalculationThread extends Thread{

    public CalculationThread (String name){
        // Pass name to Thread layer.
        super (name);
    }

    double calcPI (){
        boolean negative = true;
        double pi = 0.0;
        for (int i = 3; i < 100000; i += 2){
            if (negative)
                pi -= (1.0 / i);
            else
                pi += (1.0 / i);
            negative = !negative;
        }
        pi += 1.0;
        pi *= 4.0;
        return pi;
    }

    @Override
    public void run () {
        for (int i = 0; i < 5; i++)
            System.out.println (getName () + ": " + calcPI ());
    }

}
