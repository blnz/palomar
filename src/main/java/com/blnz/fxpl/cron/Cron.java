package com.blnz.fxpl.cron;

import com.blnz.fxpl.util.ConfigProps;

import java.util.logging.Logger;

/**
 *  The home interface for scheduled and repeating tasks.
 */
public class Cron 
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static CronService _defaultImpl = null;

    /**
     * Retrieves a configured CronService.
     *
     *  To change from the default implementation (which quietly does no scheduling), set
     *   the ConfigProps property "org.xmlecho.palomar.CronService" to the name of the implementing
     * class 
     */
    public static final CronService getCronService() 
    {
	if (_defaultImpl == null) { 
            init(); 
        } 
	return _defaultImpl;
    }
    
    /**
     *
     */
    private static synchronized void init()
    {
        String implName = "com.blnzfxpl.cron.impl.FakeCronServiceImpl";

        if (_defaultImpl != null) {
            return; // another thread beat us
        }
        try {
            implName = 
                ConfigProps.getProperty("org.xmlecho.palomar.cron.CronService",
                                        implName);
            
            _defaultImpl = 
                (CronService) Class.forName(implName).newInstance();

        } catch (Exception ex) {
            LOGGER.severe("unable to load cron service: " + implName + " " + ex.toString());
        }
    }
}
