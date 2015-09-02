package com.blnz.fxpl.core;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.xsl.om.ExtensionContext;
import com.blnz.fxpl.XProcessor;

import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A reference to a context parameter.
 *
 * upon evaluation, this results in the value of the parameter which has
 * been bound to the given name.
 */
public class ParamRefImpl extends FXRequestServerSide
{
    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {
        FXContext ctx = extendContext((FXContext)context);

        String refParamName = (String) ctx.get("refParamName");

        doEval(refParamName, responseTarget, ctx);
    }

    public static void doEval(String name, ContentHandler responseTarget, FXContext context)
        throws Exception
    {

                
        Object parmVal = context.get(name);
                
        if (parmVal == null) {
            // nothing to do here, I guess
        } else if (parmVal instanceof String) {
            String paramType = (String) context.get("paramType");
            if (paramType != null && paramType.equals("Element")) {
                XMLFilterImpl rdr = 
                    new XMLFilterImpl(XForm.createInputSourceReader()){
                        public void startDocument(){
                        }
                        
                        public void endDocument(){
                        }
                    };
                rdr.setContentHandler(responseTarget);
                rdr.parse(new InputSource(new java.io.StringReader(parmVal.toString().trim())));
                
            }
            else {
                //Treat param as PCDATA
                String text = (String) parmVal;
                char[] buf = text.toCharArray(); 
                responseTarget.characters(buf, 0, buf.length);
            }                   
        } else if (parmVal instanceof FXRequest) {
            ((FXRequest) parmVal).eval(responseTarget, context);
        } else if (parmVal instanceof XMLReader) {
            
            XMLReader rdr = (XMLReader) parmVal;
            
            rdr.setContentHandler(responseTarget);
            rdr.parse("dummy");
        }
    }
}
