// $Id: ProcessContext.java 99 2005-02-28 21:37:53Z blindsey $

package com.blnz.xsl.tr;

import com.blnz.xsl.om.*;
import com.blnz.xsl.expr.ExprContext;
import com.blnz.xsl.expr.Variant;
import com.blnz.xsl.expr.VariableSet;

import com.blnz.xsl.sax2.SaxFilterMaker;

import java.net.URL;

import java.util.Hashtable;

/**
 *  Processing context for a Stylesheet (Sheet)
 * maintains state for a transformation, and
 *  actually does the dispatching of the transformation work
 *  by calling invoke() on Actions
 */
public interface ProcessContext extends ExprContext
{
    /**
     * perform the transformation
     */
    void process(NodeIterator nodes, Name modeName,
                 Name[] paramNames, Variant[] paramValues, 
                 Result result) 
        throws XSLException;

    /**
     * 
     */
    void invoke(NodeIterator nodes, Action action, 
                Result result) 
        throws XSLException;


    /**
     * @return the parameter bound to the given name
     */
    Variant getParam(Name name) throws XSLException;


    /**
     *
     */
    void applyImports(Node node, Result result) 
        throws XSLException;


    /**
     * binds a varaible to the given Name
     */
    void bindLocalVariable(Name name, Variant variant) throws XSLException;


    /**
     *
     */
    void unbindLocalVariables(int n);


    /**
     *
     */
    void invokeWithParams(Action action, Name[] paramNames, 
                          Variant[] paramValues,
                          Node node, Result result) throws XSLException;


    /**
     *
     */
    static interface Memento
    {
        void invoke(Action action, Node node, 
                    Result result) 
            throws XSLException;
    }


    /**
     *
     */
    Memento createMemento();


    /**
     *
     */
    void useAttributeSet(Name name, Node node, 
                         Result result) 
        throws XSLException;


    /**
     *
     */
    Name unaliasName(Name name);


    /**
     *
     */
    NamespacePrefixMap unaliasNamespacePrefixMap(NamespacePrefixMap map);


    /**
     *
     */
    void put(Object key, Object value);


    /**
     *
     */
    Object get(Object key);


    /**
     *
     */
    Result createNodeResult(Node baseNode, 
                            Node[] rootNodeRef) 
        throws XSLException;

    /**
     * returns an XRAP (Extension element) processor, packaged 
     * as a SAX filter
     */
    public SaxFilterMaker getSaxExtensionFilter();

}
