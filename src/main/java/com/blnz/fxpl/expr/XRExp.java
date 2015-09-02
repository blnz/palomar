package com.blnz.fxpl.expr;

import java.io.InputStream;
import java.io.Reader;

public class XRExp  {

    private Reader _src;
    
    private XRExp (Reader r)
    {
        _src = r;
    }

    public static Expression parseExpr(Reader r)
        throws Exception
    {
        XRExp parser = new XRExp(r);
        Expression x = parser.parse();
        return x;
    }

    public Expression parse()
    {
        return null;
    }
}