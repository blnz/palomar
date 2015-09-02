package com.blnz.fxpl;

/**
 *  represent the result of evaluating an ECHO Request.
 */
public interface FXResponse extends com.blnz.xsl.om.Node
{
    /**
     */
    public boolean didSucceed();

    /**
     */
    public FXContext getResponseContext();
}
