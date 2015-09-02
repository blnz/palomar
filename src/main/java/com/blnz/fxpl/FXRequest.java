package com.blnz.fxpl;

import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;

/**
 *  The Interface for all elements in ECHO. A Node with
 *  the added method "eval()"
 */
public interface FXRequest extends com.blnz.xsl.om.NodeExtension
{

    /**
     * returns the base URI that should be associated
     * with the XML document which would result from
     * evaluating this ECHORequest in the given context
     */
    public String getURI(FXContext context);

}
