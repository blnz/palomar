package com.blnz.fxpl.log;

import com.blnz.fxpl.FXException;
import com.blnz.fxpl.util.ConfigProps;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to the configured Logging service
 *       Log Service
 */
public class  Log
{
    private static Logger _logger = null;
    private static Map<String, Logger> _loggers = new HashMap<String, Logger>();
    
    /**
     * @return an instance of the default implementation
     */
    public static final Logger getLogger()
    { 
        if (_logger == null) {
            _logger = newInstance();
        }
        return  _logger; 
    }
    
    /**
     */
    public static final Logger getLogger(String category) 
    {
        Logger logger = _loggers.get(category);
        if (logger == null) {
            logger = newInstance(category);
            _loggers.put(category, logger);
        }
        return logger;
    }
    
    private static final Logger newInstance(String category)
    {
        try {
            String implName = ConfigProps.getProperty("org.xmlecho.palomar.log.Logger");
            if (implName == null) {
                return instantiate(com.blnz.fxpl.log.impl.SimpleLogger.class, category);
            } else {
                return instantiate(Class.forName(implName), category);
            }
        } catch (Exception ex) {
            // we just want to have a fallback logger, and continue
            try {
                System.out.println("Logger configuration failed. Using SimpleLogger: " + 
                                   ex.getMessage());
                return instantiate(com.blnz.fxpl.log.impl.SimpleLogger.class, category);
            } catch (Exception e) {
                System.out.println("Error creating logger: " + e.getMessage());
                return null;
            }
        }
    }
    
    private static final Class[] constructorArgs = {String.class};
    
    private static final Logger instantiate(Class clazz, String category) throws Exception 
    {
        Constructor con = clazz.getConstructor(constructorArgs);
        return (Logger)con.newInstance(new Object[] {category});
    }
    
    private static final Logger newInstance() 
    {
        return newInstance("palomar");
    }
    
    public static int getLevel(String levelName) throws FXException {
        if (levelName == null) {
            throw new FXException("Level cannot be null");
        } else if (levelName.equals("all")) {
            return Logger.ALL;
        } else if (levelName.equals("trace")) {
            return Logger.TRACE;
        } else if (levelName.equals("debug")) {
            return Logger.DEBUG;
        } else if (levelName.equals("warn")) {
            return Logger.WARN;
        } else if (levelName.equals("info")) {
            return Logger.INFO;
        } else if (levelName.equals("error")) {
            return Logger.ERROR;
        } else if (levelName.equals("fatal")) {
            return Logger.FATAL;
        } else if (levelName.equals("none") || levelName.equals("never")) {
            return Logger.NONE;
        } else {
            throw new FXException("Unknown log level:" + levelName);
        }
    }
}
