package com.blnz.fxpl.cron;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.util.ConfigProps;

/**
 *  The home interface for scheduled and repeating tasks.
 */
public class Cron 
{
    private static CronService _defaultImpl = null;

    /**
     * Retrieves a configured CronService.
     *
     *  To change from the default implementation (which quietly does no scheduling), set
     *   the ConfigProps property "com.blnz.fxpl.CronService" to the name of the implementing
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
        String implName = "com.blnz.fxpl.cron.impl.FakeCronServiceImpl";

        if (_defaultImpl != null) {
            return; // another thread beat us
        }
        try {
            implName = 
                ConfigProps.getProperty("com.blnz.fxpl.cron.CronService",
                                        implName);
            
            _defaultImpl = 
                (CronService) Class.forName(implName).newInstance();

        } catch (Exception ex) {
            Log.getLogger().error("unable to load cron service: " + implName, ex);
        }
    }
}
