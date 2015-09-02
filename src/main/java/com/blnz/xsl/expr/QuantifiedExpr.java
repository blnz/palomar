
package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

/**
 * 
 */
class QuantifiedExpr extends ConvertibleBooleanExpr 
{
    private final Name name;
    private final VariantExpr expr1;
    private final VariantExpr expr2;

    QuantifiedExpr(Name name, VariantExpr expr1, VariantExpr expr2) 
    {
        this.name = name;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException 
    {
        return true;
    }
}

