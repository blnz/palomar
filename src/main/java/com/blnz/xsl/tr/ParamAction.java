// $Id: ParamAction.java 99 2005-02-28 21:37:53Z blindsey $

package com.blnz.xsl.tr;

import com.blnz.xsl.om.*;
import com.blnz.xsl.expr.VariantExpr;
import com.blnz.xsl.expr.Variant;
import com.blnz.xsl.expr.ExprContext;
import java.util.Hashtable;

/**
 * binds a parameter to it's value
 */
abstract class ParamAction implements Action
{
    private Name[] paramNames = null;
    private VariantExpr[] paramExprs = null;

    void addParam(Name name, VariantExpr expr) 
    {
        if (paramNames == null) {
            paramNames = new Name[]{name};
            paramExprs = new VariantExpr[]{expr};
        }
        else {
            Name[] oldParamNames = paramNames;
            paramNames = new Name[oldParamNames.length + 1];
            System.arraycopy(oldParamNames, 0, paramNames, 0, oldParamNames.length);
            paramNames[oldParamNames.length] = name;
            VariantExpr[] oldParamExprs = paramExprs;
            paramExprs = new VariantExpr[oldParamExprs.length + 1];
            System.arraycopy(oldParamExprs, 0, paramExprs, 0, oldParamExprs.length);
            paramExprs[oldParamExprs.length] = expr;
        }
    }

    Name[] getParamNames()
    {
        return paramNames;
    }

    Variant[] getParamValues(Node sourceNode, ExprContext context) throws XSLException
    {
        if (paramExprs == null) {
            return null;
        }
        Variant[] paramValues = new Variant[paramExprs.length];
        for (int i = 0; i < paramValues.length; i++) {
            paramValues[i] = paramExprs[i].eval(sourceNode, context).makePermanent();
        }
        return paramValues;
    }
}
