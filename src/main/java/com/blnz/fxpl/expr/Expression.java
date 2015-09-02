package com.blnz.fxpl.expr;

import com.blnz.fxpl.FXContext;

import org.xml.sax.ContentHandler;
import java.io.Writer;
import java.io.StringWriter;

public class Expression
{
    public static final int STRING = 1;
    public static final int INT = 2;
    public static final int NUMBER = 3;
    public static final int BOOLEAN = 4;
    public static final int DATE = 5;
    public static final int OPERATOR = 6;
    public static final int NODE = 7;
    public static final int NODESET = 8;
    public static final int COMPOUND = 9;

    public String name;

    public void eval (FXContext ctx, Writer w) throws Exception
    {
        if (name != null) {
            w.write(name);
        }
    }

    public String stringVal(FXContext ctx) throws Exception
    {
        StringWriter sw = new StringWriter();
        eval(ctx, sw);
        return sw.toString();
    }

    public boolean booleanVal(FXContext ctx) throws Exception
    {
        String s = stringVal(ctx);
        return (s != null && !"".equals(s) && !"false()".equals(s));
    }

    public double doubleVal(FXContext ctx) throws Exception
    {
        String s = stringVal(ctx);
        return Double.parseDouble(s);
    }

    public int intVal(FXContext ctx) throws Exception
    {
        String s = stringVal(ctx);
        return Integer.parseInt(s);
    }

}
