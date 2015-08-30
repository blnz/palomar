package com.blnz.palomar;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * provides context information for an ECHO processor
 *  ECHOContext objects provide the ECHO processor the ability
 *  to pass parameters to nested ECHO request handlers
 *  ECHOContext objects may contain a handle to a "parent"
 *  context.  If the requested key is not found in the requested
 *  context, it will attempt to find it in the parent context
 */
public interface ECHOContext
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

    /**
     * a parent context to delegate queries
     */ 
    public void setParentContext(ECHOContext parentContext);

    /**
     * sends a trace of the nested hierarchy of contexts
     */
    public void sendStackTrace(ContentHandler target) throws
        SAXException;

    /**
     * a trace of the nested hierarchy of contexts
     */
    public void sendStackTrace(Exception e, ContentHandler target) throws
    SAXException;

    /**
     *
     */
    public ECHOContext extend();

}
