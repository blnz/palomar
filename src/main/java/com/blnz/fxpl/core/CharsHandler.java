package com.blnz.fxpl.core;

import org.xml.sax.helpers.DefaultHandler;

/**
 * just returns the chars in an xml doc, unescaped, while stripping markup
 */
public class CharsHandler extends DefaultHandler
{
    
    String _chars = "";
    public void characters(char[] buffer, int start, int len)
    {
        _chars += new String(buffer, start, len);
    }
    
    public String getString()
    { return _chars; }
    
}

