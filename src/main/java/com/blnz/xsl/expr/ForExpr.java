package com.blnz.xsl.expr;

import java.util.ArrayList;

import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.Node;
import com.blnz.xsl.om.XSLException;

/**
 * 
 */
class ForExpr extends ConvertibleBooleanExpr 
{
    private final ArrayList<Name> _names;
    private final VariantExpr _expr1;
    private final VariantExpr _expr2;

    ForExpr(ArrayList<Name> names, VariantExpr expr1, VariantExpr expr2) 
    {
        this._names = names;
        this._expr1 = expr1;
        this._expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException 
    {
        return true;
    }
}

