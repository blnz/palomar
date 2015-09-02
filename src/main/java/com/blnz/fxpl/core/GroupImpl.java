package com.blnz.fxpl.core;

import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.ExtensionContext;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;



/**
 * represents a request to construct an element
 */
public class GroupImpl extends FXRequestServerSide
{
    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws Exception
    {

        FXContext ctx = extendContext((FXContext) context);

        try {

            //            SafeNodeIterator kids = getChildNodes();
            
            String groupName = (String) ctx.get("elementName");
            if (groupName == null || "".equals(groupName)) {
                // deprecated param name
                groupName = (String) ctx.get("groupName");
            }
            
            String groupNS = (String) ctx.get("elementNS");
            if (groupNS == null || "".equals(groupNS)) {
                // deprecated param name
                groupNS = (String) ctx.get("groupNS");
            }

            String groupNSPrefix = (String) ctx.get("elementNSPrefix");
            if (groupNSPrefix == null || "".equals(groupNSPrefix)) {
                // deprecated param name
                groupNSPrefix = (String) ctx.get("groupNSPrefix");
            }

            String qname = null;

            String localName = this.getNode().getName().getLocalPart();

            if ( ( FXHome.NAMESPACE.equals(getNamespaceURI()) || 
                   "http://namespaces.blnz.com/fxpl".equals(getNamespaceURI()) ) &&
                 ( "xrap".equals(localName) ||
                   "echo".equals(localName) ||
                   "trim".equals(localName) ) ) {
               // nuttin
            } else {
                if (groupName != null && groupName.length() > 0) {
                    if (groupNSPrefix != null && groupNSPrefix.length() > 0) {
                        if (!groupNSPrefix.endsWith(":")) {
                            qname = groupNSPrefix + ":" + groupName;
                        } else {
                            qname = groupNSPrefix + groupName;
                        } 
                    } else {
                        qname = groupName;
                    }
                }
            }

            // FIXME: pull out extra attribute definitions? 
            
            evalBody(responseTarget, ctx, groupNS, 
                     groupName, qname, new AttributesImpl());
        } catch (Exception ex) {
	    errorResponse(ex, responseTarget, ctx);
        }
    }
}


