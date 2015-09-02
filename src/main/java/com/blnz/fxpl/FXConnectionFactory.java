package com.blnz.fxpl;

/**
 * Factory interface for obtaining an FXPL XProcessor
 *  configured with an initial set of User credentails
 */
public interface FXConnectionFactory
{
    /**
     * Obtain an FXPL Processor.
     */
    public XProcessor getXProcessor(String svcName);
    
    /**
     * Obtain a connection to an ECHO Processor 
     */
    public FXConnection getFXConnection(String serviceName,
                                        String userName,
                                        String userCredential)
        throws FXException;
    
}
