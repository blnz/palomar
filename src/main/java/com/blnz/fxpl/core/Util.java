package com.blnz.fxpl.core;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.SafeNodeIterator;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.NameTableImpl;
import com.blnz.xsl.om.Name;

public class Util
{

    public static final String getAttributeValue(Node element, String attributeName)
    {
        return element.getAttributeValue(((NameTableImpl) element.getName().getCreator()).createName(attributeName));
    }

    public static final Node rootElement(Node n)
    {
        if (n == null) {
            return null;
        }
        Node root = n.getRoot();
        if (root == null) {
            return null;
        }
        SafeNodeIterator ni = root.getChildren();
        Node el = ni.next();
        while ( el != null && el.getType() != Node.ELEMENT) {
            el = ni.next();
        }
        return el;
    }
}


