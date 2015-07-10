package com.acme.lang;

import com.acme.lang.model.UpShared;
import com.acme.lang.thread.Consumer;
import com.acme.lang.thread.Producer;

public class CorrectProdCons {

    public static void main (String [] args)
    {
        UpShared s = new UpShared ();
        new Producer(s).start ();
        new Consumer(s).start ();
    }

}
