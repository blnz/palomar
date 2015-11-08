package com.blnz.fxpl.core;

import com.blnz.fxpl.FXContext;

import com.blnz.xsl.expr.BooleanExpr;
import com.blnz.xsl.expr.EmptyVariableSet;
import com.blnz.xsl.expr.Expr2Parser;
import com.blnz.xsl.om.ExtensionContext;

import org.xml.sax.ContentHandler;

/**
 * fetch an xml document from the repository, and evaluate it as FXPL
 */
public class IfImpl extends FXRequestServerSide
{

    BooleanExpr _expr = null;

    /**
     * evaluate the request
     */
    public void eval(ContentHandler responseTarget, ExtensionContext context) 
        throws Exception
    {
        FXContext ctx = extendContext((FXContext) context);

        if (_expr == null) {
            String testVal = getAttributeValue("test");
            _expr = Expr2Parser.parseBooleanExpr(getNode(), testVal, new EmptyVariableSet());
        }
        
        if (_expr.eval(this.getNode(), (FXContextImpl)ctx)) {
            processChildren(responseTarget, ctx, true);
        }
    }
 }
