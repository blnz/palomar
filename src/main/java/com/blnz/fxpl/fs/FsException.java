package com.blnz.fxpl.fs;

import com.blnz.fxpl.FXException;

/**
 * May be used to wrap other Exceptions
 */
public class FsException extends FXException
{
    
    public FsException()
    { super(); }

    /** 
     * create a new XfsException with the given message string
     */
    public FsException (String msg)
    { super(msg); }

    /**
     *  create a new XfsException wrapped over another Exception 
     * and a message 
     */
    public FsException(String msg, Exception why) 
    { super(msg, why); }

    /** 
     * create a new XfsException wrapped over another Exception 
     */
    public FsException(Exception why)
    { super(why);}

}
