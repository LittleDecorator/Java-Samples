package com.acme.lang.thread;

public class BlockingThread extends Thread {
    private boolean finished = false;

    @Override
    public void run (){
        while (!finished){
            try{
                int i;
                do{
                    i = System.in.read ();
                    System.out.print (i + " ");
                }
                while (i != '\n');
                System.out.print ('\n');
            } catch (java.io.IOException e) {}
        }
    }
    public void setFinished (boolean f)
    {
        finished = f;
    }
}
