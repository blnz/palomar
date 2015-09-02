package com.blnz.fxpl.core;

import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;

/**
 * Represents Request to do nothing.
 *
 * A NullTransform produces no output, and does not cause its
 * child Elements to be evaluated.
 */
public class NullTransformImpl extends FXRequestImpl
{
    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {
        // transforms to  NOTHING!
    }

}


