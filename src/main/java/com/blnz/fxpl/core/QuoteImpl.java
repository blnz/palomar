package com.blnz.fxpl.core;

import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.Node;

import com.blnz.xsl.om.ExtensionContext;

import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class QuoteImpl extends FXRequestServerSide
{

    
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
    throws Exception
{

     SafeNodeIterator kids = this.getNode().getChildren();
     responseTarget.startDocument();
     Node n = kids.next();
     while (n != null) {
         if (n.getType() == Node.ELEMENT) {
             
             XMLReader reader =  
                 XForm.createOMSourceReader(n);
             
             reader.setContentHandler(responseTarget);
             reader.parse(new InputSource(getURI((FXContext)context)));
         }
         
         n = kids.next();
     }  
    responseTarget.endDocument();
}


}
