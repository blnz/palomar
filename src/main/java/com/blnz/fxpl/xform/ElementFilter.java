package com.blnz.fxpl.xform;

import java.util.Hashtable;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStream;

import org.xml.sax.DocumentHandler;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;

import com.blnz.fxpl.xform.DownstreamProcessStatus;


/**
 * A streaming filter that selectively removes Elements
 * from the input
 */

public interface ElementFilter extends Transformer
{
    public void setTester(ElementTest tester);


    public void setDownstreamProcessStatus(DownstreamProcessStatus indicator);

    /**
     * boolean negation of an element test
     */
    public ElementTest createNotTest(ElementTest who); 

    /**
     * always tests false
     */
    public ElementTest createFalseTest(); 

    /**
     * always tests true
     */
    public ElementTest createTrueTest(); 

    /**
     *
     */
    public ElementTest createAndTest(ElementTest one, 
                                     ElementTest two);


    /**
     *
     */
    public ElementTest createOrTest(ElementTest one, 
                                    ElementTest two);

    /**
     * @return true if the attribute exists on an element
     *
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestExist(String namespace,
                                           String name);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestLT(String namespace,
                                        String name,
                                        String testval);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestGT(String namespace,
                                        String name,
                                        String testval);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestEQ(String namespace,
                                        String name,
                                        String testval);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestLE(String namespace,
                                        String name,
                                        String testval);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestGE(String namespace,
                                        String name,
                                        String testval);
    /**
     * Performs a test of the named attribute's value against the given
     *  regular expression
     *
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createAttrTestRegex(String namespace,
                                           String name,
                                           String regex);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     */
    public ElementTest createElemNameTest(String namespace,
                                          String name);

    /**
     * @param namespace the attribute name's namespace
     * @param name the attribute name's local name
     * @param skipCount number of  elements to skip
     * @param passCount number of elements to pass
     */
    public ElementTest createElemCountTest(String namespace,
                                           String name,
                                           int skipCount,
                                           int passCount,
                                           int passDepth);

}

