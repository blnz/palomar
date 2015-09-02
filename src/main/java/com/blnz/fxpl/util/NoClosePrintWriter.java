// $Id: NoClosePrintWriter.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.util;

import java.io.PrintWriter;

/** 
 *  a PrintWriter that just won't close ... 
 */
public class NoClosePrintWriter extends PrintWriter
{
    private boolean _error = false;

    /**
     * @param out The ultimate destination of the output stream
     */
    public NoClosePrintWriter(PrintWriter out)
    {
        super(out); 
    }

    /** 
     * flush the output, but do not close
     */
    public void close() 
    {
	super.flush();
    }
    
    /**
     *  determine if an error has occurred during processing 
     */
    public boolean checkError() 
    { return _error; }
    
    /** 
     *        Indicate that an error has occurred. 
     */
    protected  void setError() 
    { _error = true; }
}
