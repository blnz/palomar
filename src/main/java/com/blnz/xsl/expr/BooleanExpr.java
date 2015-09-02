
package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

public interface BooleanExpr
{
    boolean eval(Node node, ExprContext context) throws XSLException;
}
