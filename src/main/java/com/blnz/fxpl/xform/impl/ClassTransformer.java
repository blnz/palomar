package com.blnz.fxpl.xform.impl;

import com.blnz.fxpl.xform.TransformException;

import java.util.Enumeration;
import java.io.FileReader;

import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.AttributeList;

/**
 *  The compiled stylesheet Transformer.
 *  Transforms a document with the provided stylesheet.
 */
public class ClassTransformer extends TransformerImplBase
{
    /**
     * Loads a specific stylesheet class
     * @exception	TransformException	raised while loading class
     */
    private void loadStyleSheetClass() throws TransformException
    {
        //          String className = _stylesheetURI.substring(0,_stylesheetURI.indexOf ('.'));
        //          try {
        //              System.out.println(" class name = " + className);
        //              _styleSheetClass = Class.forName(className);
        //              _styleSheetTranslet = (Translet) _styleSheetClass.newInstance();
        //          } catch(ClassNotFoundException cfE) {
        //              cfE.printStackTrace();
        //              throw new TransformException(cfE.getMessage());
        //          }
        //          catch(IllegalAccessException iaE) {
        //              throw new TransformException(iaE.getMessage());
        //          }
        //          catch(InstantiationException iE) {
        //              throw new TransformException(iE.getMessage());
        //          }
    }
  
  
    /**
     *  Adds parameters before transforming.
     *  @see	java.util.Enumeration
     */
    private void addParams()
    {
        Enumeration penum = _params.elements();
        while( penum.hasMoreElements() ) {
            String nextParam = (String)penum.nextElement();
            //            _styleSheetTranslet.addParameter(nextParam,_params.get(nextParam) );
        }
    }
  
  
    /**
     * Transforms the input document.
     * @see		ClassTransformer#loadStyleSheetClass()
     * @see		StyleSheetTransformer#setXslProcessorParams()
     * @see		com.sun.xslt.dom.DOM
     * @see		com.sun.xml.parser.Parser
     * @exception	TransformException
     */
    public void doTransform() throws TransformException
    {
        //      try {
        //          loadStyleSheetClass();
        
        //          // Arghhh ...
        //          System.out.println(" class loaded...");
        //          Parser parser = new Parser();
        //          DOM dom = new DOM();
        //          parser.setDocumentHandler(dom.getBuilder());
        //          parser.parse(_docInputSource);
        //          addParams();
        //          //printDOM(dom);
        //          // Transform the document
        //          _styleSheetTranslet.transform(dom, (HandlerBase)_handler);
        //        } catch (Exception e) {
        //          e.printStackTrace();
        //          System.err.println("Ravi Error: internal error.");
        //          throw new TransformException(e.getMessage() );
        //        }
      
    }
  
  
}
