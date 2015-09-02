package com.blnz.xsl.expr;

import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.NodeIterator;
import com.blnz.xsl.om.XSLException;

/**
 *
 */
class IntersectExpr extends ConvertibleNodeSetExpr 
{
    private final NodeSetExpr expr1;
    private final NodeSetExpr expr2;
    
    IntersectExpr(NodeSetExpr expr1, NodeSetExpr expr2) 
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public NodeIterator eval(Node node, ExprContext context) throws XSLException 
    {
        return new IntersectionNodeIterator(expr1.eval(node, context),
                                     expr2.eval(node, context));
    }
}
