package com.blnz.fxpl.log.impl;

import com.blnz.fxpl.log.Logger;

/**
 *
 */
public class SimpleLogger implements Logger 
{
    
    private static final String DEBUG = "DEBUG";
    private static final String WARN = "WARN";
    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String FATAL = "FATAL";
    
    private String category;
    
    public SimpleLogger(String category) 
    {
        this.category = category;
    }
    
    public void debug(String msg) {
        // FIXME: make this guy understand log levels
        //         message(DEBUG, msg);
    }

    public void warn(String msg) 
    {
        message(WARN, msg);
    }

    public void info(String msg) 
    {
        message(INFO, msg);
    }

    public void error(String msg) 
    {
        message(ERROR, msg);
    }

    public void fatal(String msg) 
    {
        message(FATAL, msg);
    }

    public void debug(String msg, Throwable t) 
    {
        message(DEBUG, msg);
    }

    public void warn(String msg, Throwable t) 
    {
       message(WARN, msg);
    }

    public void info(String msg, Throwable t) 
    {
       message(INFO, msg);
    }

    public void error(String msg, Throwable t) 
    {
       message(ERROR, msg);
    }

    public void fatal(String msg, Throwable t) 
    {
       message(FATAL, msg);
    }

    public boolean isDebugEnabled() 
    {
        // FIXME 
        return false;
    }

    private int getLevel() 
    {
        return Logger.WARN;
    }
    
    public boolean isLogging(int level) 
    {
        return level >= getLevel();
    }

    private void message(String type, String msg) 
    {
        System.err.println(type + "/" + category + ": " + msg);
    }
}
