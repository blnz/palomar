package com.blnz.fxpl.log;

/**
 *
 */
public interface Logger 
{

    public static final int ALL = 1;
    public static final int TRACE = 2;
    public static final int DEBUG = 3;
    public static final int WARN = 4;
    public static final int INFO = 5;
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    public static final int NONE = 8;
    
    public void debug (String msg);
    public void warn (String msg);
    public void info (String msg);
    public void error (String msg);
    public void fatal (String msg);
    public void debug (String msg, Throwable t);
    public void warn (String msg, Throwable t);
    public void info (String msg, Throwable t);
    public void error (String msg, Throwable t);
    public void fatal (String msg, Throwable t);
    
    public boolean isDebugEnabled();
    public boolean isLogging(int level);
}
