package com.acme.lang.thread;

public class SummingThread extends Thread
{
    private boolean finished = false;
    public void run ()
    {
        int sum = 0;
        while (!finished)
            sum++;
    }
    public void setFinished (boolean f)
    {
        finished = f;
    }
}
