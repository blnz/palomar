// $Id: NoCloseOutputStream.java 155 2005-04-27 01:03:47Z blindsey $

package com.blnz.fxpl.util;

import java.io.OutputStream;
import java.io.FilterOutputStream;

/** 
 * an OutputStream that just won't close 
 */
public class NoCloseOutputStream extends FilterOutputStream
{
    /**
     */
    public NoCloseOutputStream(OutputStream out)
    {
        super(out); 
    }

    /** 
     * flush() the output, but do not close
     */
    public void close() throws java.io.IOException
    {
	super.flush();
    }
}
