package com.blnz.fxpl.dom;

import org.xml.sax.Locator;

/**
 *
 */
public class Location implements Locator
{
    public int columnNumber = -1;
    public int lineNumber = -1;
    public String publicId = null;
    public String systemId = null;
    
    public Location(Locator where)
    { 
        columnNumber = where.getColumnNumber();
        lineNumber = where.getLineNumber();
        publicId = (where.getPublicId() == null) ? "" : where.getPublicId();
        systemId = (where.getSystemId() == null) ? "" : where.getSystemId();
    }

    public int getColumnNumber()
    {
        return columnNumber;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getPublicId()
    {
        return publicId;
    }

    public String getSystemId()
    {
        return systemId;
    }
}
