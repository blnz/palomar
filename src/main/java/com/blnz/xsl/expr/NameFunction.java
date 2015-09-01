// $Id: NameFunction.java 96 2005-02-28 21:07:29Z blindsey $

package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

class NameFunction extends FunctionOpt1
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr expr) throws ParseException
    {
        final NodeSetExpr nse = expr.makeNodeSetExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException
                {
                    node = nse.eval(node, context).next();
                    if (node != null) {
                        Name name = node.getName();
                        if (name != null)
                            return name.toString();
                    }
                    return "";
                }
            };
    }
}