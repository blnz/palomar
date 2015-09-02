package com.blnz.fxpl.core;

import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.Parameter;
import com.blnz.fxpl.log.Log;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeIterator;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;

/**
 * represents a set of parameters to an FX request
 */
public class ParamSetImpl extends FXRequestServerSide 
{
    /**
     * @return list of parameters defined in the paramSet. each parameter
     * object includes parameter name, type, and (possibly) a value.
     */

    /**
     * returns the set of bindings in a ParamSet as a hashtable
     */
    public Hashtable getHashtable(FXContext context) 
        throws Exception 
    {
        
        Hashtable ht = new Hashtable();

        //         NodeList nl = this.getElementsByTagNameNS(FXHome.NAMESPACE,
        //                                                   "param");

        NodeIterator nl = this.getNode().getChildren();
        
        Node e = nl.next();
        
        while (e != null) {
            if (e.getNodeExtension() instanceof FXParamImpl) {
                String name = Util.getAttributeValue(e, "name");
                boolean getRaw =  "true".equals(Util.getAttributeValue(e, "getRaw"));
                
                String val = "";
                try {
                    val = (String) ( getRaw ? ((FXContextImpl)context).getRaw(name) : context.get(name)  ) ;
                } catch (ClassCastException ex) {
                    Log.getLogger().warn(name + " is not a String value", ex);
                }

                //lte: Added for stylesheets that'll need the user's ID
                if (name.equals("userID")) {
                    val = String.valueOf(getUser(context).getID());
                }
                
                // FIXME: handle case when we need to evaluate binding contents
                if (val != null && val.length() > 0) {
                    ht.put(name, val);
                } else {
                    //                    ht.put(name, "");
                }
            }
            e = nl.next();
        }
        return ht;
    }

}
