package com.acme.lang.model;

public class UpShared implements Share{

    private char c = '\u0000';
    private boolean writeable = true;

    public synchronized void setSharedChar (char c)
    {
        while (!writeable)
            try
            {
                wait ();
            }
            catch (InterruptedException e) {}

        this.c = c;
        writeable = false;
        notify ();
    }

    public synchronized char getSharedChar ()
    {
        while (writeable)
            try
            {
                wait ();
            }
            catch (InterruptedException e) { }

        writeable = true;
        notify ();

        return c;
    }

}
