// $Id: ModuloExpr.java 112 2005-03-28 21:39:11Z blindsey $

package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

class ModuloExpr extends ConvertibleNumberExpr 
{
    private final NumberExpr expr1;
    private final NumberExpr expr2;
    
    ModuloExpr(NumberExpr expr1, NumberExpr expr2) 
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    public double eval(Node node, ExprContext context)
        throws XSLException 
    {
        return expr1.eval(node, context) % expr2.eval(node, context);
    }
}
