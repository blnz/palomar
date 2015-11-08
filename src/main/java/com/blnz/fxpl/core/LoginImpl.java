package com.blnz.fxpl.core;

import com.blnz.fxpl.security.SecurityService;
import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;

import com.blnz.fxpl.*;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;


/**
 * represents an XRAP request to establish a new session
 */
public class LoginImpl extends FXRequestServerSide
{
    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
	throws FXException, SAXException
    {
        if (responseTarget == null) {
            throw new FXException("null response target for login");
        }
        
        String userName = getAttributeValue("userName");
        if (userName == null) {
            throw new FXException("null userName for login");
        }
        
        String password = getAttributeValue("password");
        if (password == null) {
            throw new FXException("null password for login");
        }
        
        String hostInfo = null;
        if (context == null) {
            hostInfo = "123";
        } else {
            hostInfo = (String) context.get("hostInfo");
        }

        SecurityService ss = Security.getSecurityService();

        User user = ss.login(userName, password);

        // FIXME: get real values for these guys
        String certificate = "12acb036f3";
        String sessionID = "124257";

        // FIXME: get these namespaces right
        AttributesImpl atts = new AttributesImpl();
        if ( user == null) {
            if (false) {
                atts.addAttribute("", "type", "type", "CDATA", "Login Failed");
                
                responseTarget.startElement(FXHome.NAMESPACE, 
                                            "error", "error", atts);
                
                responseTarget.endElement(FXHome.NAMESPACE, 
                                          "error", "error");

            } else {

                // FIXME: temp hack
                String userID = "42";
                atts.addAttribute("", "userID", "userID", "CDATA", userID);
                atts.addAttribute("", "path", "path", "CDATA", "/");
                atts.addAttribute("", "connectionID", "connectionID", "CDATA", sessionID);
                atts.addAttribute("", "certificate", "certificate", "CDATA", 
                                  certificate);
                
                responseTarget.startElement(FXHome.NAMESPACE, "context", 
                                            "context", atts);
                
                responseTarget.endElement(FXHome.NAMESPACE, "context", "context");
            }

        } else {
            String userID = user.getID();

            atts.addAttribute("", "userID", "userID", "CDATA", userID);
            atts.addAttribute("", "path", "path", "CDATA", "/");
            atts.addAttribute("", "connectionID", "connectionID", "CDATA", sessionID);
            atts.addAttribute("", "certificate", "certificate", "CDATA", 
                              certificate);

            responseTarget.startElement(FXHome.NAMESPACE, "context", 
                                        "context", atts);

            responseTarget.endElement(FXHome.NAMESPACE, "context", "context");
        }
        //        responseTarget.endDocument();
    }
}

