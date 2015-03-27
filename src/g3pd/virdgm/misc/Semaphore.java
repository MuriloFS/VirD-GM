/*
 * Semaphore.java
 *
 * Created on 16/11/2007, 16:24:57
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package g3pd.virdgm.misc;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giulian
 */
public class Semaphore {
    private int count;

    public Semaphore() {
        this(0);
    }

    public Semaphore(int value) {
        count = value;
    }

    synchronized public void p() {
        while (count == 0) {
            try {
            	//VirdLogger.timeLogger("NO AGUARDO",1);
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Semaphore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        count--;
    }

    synchronized public void v() {
        if (count == 0) {
            notify();
        }

        count++;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
