package com.acme.lang;

import com.acme.lang.model.Shared;
import com.acme.lang.thread.Consumer;
import com.acme.lang.thread.Producer;

public class WrongProdCons {

    public static void main (String [] args)
    {
        Shared s = new Shared();
        new Producer(s).start ();
        new Consumer(s).start ();
    }

}
