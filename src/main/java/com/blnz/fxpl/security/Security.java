package com.blnz.fxpl.security;

import com.blnz.fxpl.util.ConfigProps;

/**
 * provides access to the configured
 * Security Service implementations
 */
public class Security
{
    private static SecurityService _ss = null;

    /**
     * Retrieves configured Security Service implementation
     *@return the SecurityService implementation object
     */
    public static final SecurityService getSecurityService()
    { 
        if (_ss == null) { 
            init(); 
        } 
        return _ss; 
    }

    /**
     * configure with this runtime's implementing service
     * class as specified in the property 
     * "com.blnz.fxpl.security.SecurityService"
     */
    private static synchronized void init()
    {
        String implName = null;
        
        if (_ss != null) {
            return; // another thread beat us
        }
        try {
            implName = 
                ConfigProps.getProperty("com.blnz.fxpl.security.SecurityService",
                                        "com.blnz.fxpl.security.impl.SecurityServiceImpl");
            
            _ss = (SecurityService) Class.forName(implName).newInstance();
            
        } catch (Exception ex) {
            // we just want to have a fallback, and continue
            _ss = new com.blnz.fxpl.security.impl.SecurityServiceImpl();
        }
    }
}




