package com.blnz.xsl.om;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

/**
 * 
 */
public interface ExtensionContext
{
    /**
     * @return the value identified by <code>key</code>
     * @param key the name of the requested value
     */
    public Object get(String key);

    /**
     * stores the value in the local context
     */
    public void put(String key, Object value);

    /**
     * removes an object from the context.
     */
    public Object remove(String key);


}
