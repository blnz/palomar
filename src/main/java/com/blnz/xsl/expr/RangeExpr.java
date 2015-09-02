package com.blnz.xsl.expr;
import com.blnz.xsl.om.*;


/**
 *
 */
class RangeExpr extends ConvertibleVariantExpr
{
   
    private final VariantExpr _expr1;
    private final VariantExpr _expr2;

    /**
     * construct with a test and two sub-expressions
     */
    RangeExpr(
           VariantExpr expr1, 
           VariantExpr expr2) 
    {
      
        this._expr1 = expr1;
        this._expr2 = expr2;
    }
    
    /**
     * evaluate with a context node and an expression context
     */
    public Variant eval(Node node, 
                        ExprContext context) 
        throws XSLException 
    {
        // FIXME:
        return null;
    }

}
