// $Id: ParentAxisExpr.java 96 2005-02-28 21:07:29Z blindsey $

package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

/**
 *
 */
class ParentAxisExpr extends AxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) 
    {
        return new SingleNodeIterator(node.getParent());
    }
    
    int getOptimizeFlags() 
    {
        return SINGLE_LEVEL;
    }
}
