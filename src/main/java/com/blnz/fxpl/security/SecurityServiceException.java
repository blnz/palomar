package com.blnz.fxpl.security;

import com.blnz.fxpl.FXException;

/**
 * May be used to wrap other Exceptions
 */
public class SecurityServiceException extends FXException
{
    
    public SecurityServiceException()
    { super(); }

    /** 
     * Construct a new SecurityException with the given message string
     */
    public SecurityServiceException (String msg)
    { super(msg); }

    /** 
     * Construct a new SecurityException wrapped over another Exception 
     * and a message
     */
    public SecurityServiceException(String msg, Exception why) 
    { super(msg, why); }

    /** 
     * Construct a new SecurityException wrapped over another Exception 
     */
    public SecurityServiceException(Exception why)
    { super(why);}

}
