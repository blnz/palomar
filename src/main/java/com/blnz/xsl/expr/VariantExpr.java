
package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

/**
 *
 */
public interface VariantExpr 
{
    Variant eval(Node node, ExprContext context) throws XSLException;
}
