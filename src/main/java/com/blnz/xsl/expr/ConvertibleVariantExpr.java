// $Id: ConvertibleVariantExpr.java 96 2005-02-28 21:07:29Z blindsey $

package com.blnz.xsl.expr;

import com.blnz.xsl.om.*;

/**
 *
 */
abstract class ConvertibleVariantExpr 
    extends ConvertibleExpr implements VariantExpr
{

    ConvertibleVariantExpr makeVariantExpr()
    {
        return this;
    }

    ConvertibleBooleanExpr makePredicateExpr()
    {
        return new ConvertibleBooleanExpr() 
            {
                public boolean eval(Node node, ExprContext context) 
                    throws XSLException 
                {
                    return ConvertibleVariantExpr.this.eval(node, context).convertToPredicate(context);
                }
            };
    }

    ConvertibleBooleanExpr makeBooleanExpr()
    {
        return new ConvertibleBooleanExpr() 
            {
                public boolean eval(Node node, ExprContext context) 
                    throws XSLException 
                {
                    return ConvertibleVariantExpr.this.eval(node, context).convertToBoolean();
                }
            };
    }

    ConvertibleNumberExpr makeNumberExpr()
    {
        return new ConvertibleNumberExpr() 
            {
                public double eval(Node node, ExprContext context) 
                    throws XSLException 
                {
                    return ConvertibleVariantExpr.this.eval(node, 
                                                            context).convertToNumber();
                }
            };
    }

    ConvertibleStringExpr makeStringExpr()
    {
        return new ConvertibleStringExpr() 
            {
                public String eval(Node node, 
                                   ExprContext context) throws XSLException
                {
                    return ConvertibleVariantExpr.this.eval(node, 
                                                            context).convertToString();
                }
            };
    }

    ConvertibleNodeSetExpr makeNodeSetExpr()
    {
        return new ConvertibleNodeSetExpr()
            {
                public NodeIterator eval(Node node, 
                                         ExprContext context) throws XSLException
                {
                    return ConvertibleVariantExpr.this.eval(node, context).convertToNodeSet();
                }
            };
    }
}
