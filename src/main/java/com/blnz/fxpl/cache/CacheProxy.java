package com.blnz.fxpl.cache;

import com.blnz.fxpl.security.SecurityService;
import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;

import com.blnz.fxpl.cron.Cron;
import com.blnz.fxpl.cron.CronService;
import com.blnz.fxpl.cron.Refreshable;

import com.blnz.fxpl.util.Base64Encode;
import com.blnz.fxpl.util.ConfigProps;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import java.util.Vector;
import java.util.Date;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


/**
 * Proxy class for managing <b>Palomar</b> objects cache.
 * All cache requests should go through this proxy object.
 * @see Cache
 * @see Policy
 * @see Holder
 */

public class CacheProxy implements CacheService
{
    /*
      NOTE: Policies can  be set explicitly on objects. If no policies
      are set, default policies are applied.
    */
    
    /** 
     * The cache properties Hashtable .Has Mapping of type vs Properties( which
     *  has the specific type's name/value pair properties )
     */
    private Hashtable _cacheProperties = new Hashtable();
    
    /**
     *  Contains mappings of type vs specific cache(smartcache or dumbcaches).
     * <p><pre>
     *  Ex: xmlnames - IrisDumbCache
     *      bo       - IrisSmartCache
     *      xform    - IrisSmartCache 
     *  </pre></p>
     */
    private Hashtable _cacheTable = new Hashtable();

    //    private static CacheProxy _instance = null;

    private Vector _partnerNotifiers = new Vector();

    // controller for monitoring caches
    // private Monitor cacheMonitor = null;
    
    protected long cachesDefRefreshFreq;
    
    protected int cachesDefMaxsize;    

    /** 
     * 
     */
    public CacheProxy() 
    {
        try {
            initialize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *  Method initializes the default caches.
     */
    private void initialize() throws Exception 
    {
        CachePropertiesLoader propLoader = new CachePropertiesLoader();
        propLoader.loadProperties(); //load properties
        _cacheProperties = propLoader.getCachesTable();

        CronService cs = Cron.getCronService();

        setInitialCache(_cacheProperties, cs);
        
    } 
    
    /**
     *  Returns the indicated <code>Cache</code>.
     *
     * @return the named cache, or <code>null</code> if none exists with the given name.
     */
    public Cache getCache(String cacheName)
    {
        return (Cache) _cacheTable.get(cacheName);
    }
    
    /**
     *  Method for setting the initial cache properties
     */
    private void setInitialCache(Hashtable initialTable,
                                 CronService cs)
        throws Exception
    {
        for (Enumeration totalTypes = initialTable.keys(); 
             totalTypes.hasMoreElements() ;
             ) {
            
            String nextType = (String)totalTypes.nextElement();
            Properties nextTypeProps = (Properties)initialTable.get(nextType);
            String nextTypeClassName = 
                (String)nextTypeProps.get("palomar.CacheProxy.cache." + 
                                          nextType + ".class");
            
            Class nextTypeClass = null;
            try {
                nextTypeClass = Class.forName(nextTypeClassName); 
            } catch (ClassNotFoundException cnfE) {
                
                throw cnfE;
            }
            
            // create an instance , set the properties and update the proxy table
            Cache nextCache = (Cache) nextTypeClass.newInstance();
            
            nextCache.setRefreshFreq(cachesDefRefreshFreq);
            
            nextCache.setMaxCacheSize(cachesDefMaxsize);           
            nextCache.setProperties( nextType, nextTypeProps );            

            _cacheTable.put(nextType , nextCache );

            // let CronService monitor this guy
            cs.add(nextCache);
        }
    }
    
    /** 
     * Method for generating an object policies based on its properties 
     * @deprecated  ********
     */
    private Policy[] getDefaultPolicies(String type,
                                        Hashtable props)
    {
        long ttlPeriod = 
            Long.parseLong((String) props.get(type + 
                                              ".object.policy_ttl") );
        
        // System.err.println( " ttl policy time " + props.get(type".object)
        Policy[] defaultPolicies = { new TTLPolicy(ttlPeriod) };
        return defaultPolicies;
    }


    //----------------  Admin Interfaces -----------------------------------   
    
    /** 
     *  Cleans the previously cached strains, if any.
     */
    /* NOTE:If admin requires a cleaning interface, this function can be exposed
       through a public interface 
    */
    private void delPersistentFiles()
    {
        // String cacheDir = IrisResourceBroker.getBroker().getFileProxy().getCacheDir();    
        String root = 
            ConfigProps.getProperty("com.snapbridge.fed.broker.IrisFileProxy.rootPath");
        String cacheUri = 
            ConfigProps.getProperty("com.snapbridge.fed.broker.IrisFileProxy.cacheSubpath");
        String cacheDir = 
            root + System.getProperty("file.separator") + cacheUri;
        try {
            ConfigProps.getFiles().deleteDir(cacheDir);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //Note: Currently cleaning is only deleting files from persistent storage
    // more to be added.
    private void cleanCaches()
    {
        delPersistentFiles();
    }
    

    /**
     * notify clustered servers that an object has been invalidated in a named cache
     */
    public void notifyPartners(String cacheName, String objectKey)
    {
        Enumeration en = _partnerNotifiers.elements();

        while (en.hasMoreElements()) {
            PartnerNotifier pn = (PartnerNotifier) en.nextElement();
            pn.notify(cacheName, objectKey);
        }
    }
    

    /**
     * notify clustered servers that an object has been invalidated in a named cache
     */
    public boolean shouldNotify(String cacheName)
    {
        Enumeration en = _partnerNotifiers.elements();

        while (en.hasMoreElements()) {
            PartnerNotifier pn = (PartnerNotifier) en.nextElement();
            if (pn.shouldNotify(cacheName) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Refresh one or more caches. 
     *
     * @param cacheList   list of caches to be cleaned
     */
    public void refresh(Vector cacheList)
    {
        synchronized (this) {
            for (Enumeration cacheTypes = cacheList.elements(); 
                 cacheTypes.hasMoreElements() ;
                 ) {
                
                String nextCacheType = (String) cacheTypes.nextElement();
                Cache nextCache = (Cache)_cacheTable.get(nextCacheType);
                
                //    System.err.println( " Invoking clear() on cache =" + nextCacheType );
                if (nextCache == null) {
                    System.err.println( "null cache =" + nextCacheType );
                } else {
                    nextCache.clear();
                }
            }
        }
    }
    
    /**
     * Refresh the entire fdx cache area.
     */
    public void refresh()
    {
        Vector totalList = getCachesVec();
        refresh(totalList);
    }

    /**
     *
     */
    private Vector getCachesVec()
    {
        Vector temp = new Vector();
        for (Enumeration cacheTypes = _cacheTable.keys(); 
             cacheTypes.hasMoreElements() ;
             ) {

            temp.addElement(cacheTypes.nextElement() );

        }
        return temp;
    }
    
    /**
     *
     */
    public Map getCacheProperties()
    {
        Vector totalList = getCachesVec();
        return getCacheDetails(totalList);
    }
    
    /**
     *
     */
    public Map getCacheDetails(Vector cacheList)
    {
        Hashtable returnTable = new Hashtable();
        for (Enumeration cacheTypes = cacheList.elements(); 
             cacheTypes.hasMoreElements() ;
             ) {
            String nextCacheType = (String) cacheTypes.nextElement();
            Cache nextCache = (Cache) _cacheTable.get(nextCacheType); 
            returnTable.put(nextCacheType, nextCache.getProperties() );
        }
        return returnTable;
                   
    }
    
    //-----------------------------------------------------------
    
    /**
     *  Utility class for loading cache properties.
     */
    private class CachePropertiesLoader
    {
        /**
         * contains types as keys and values(actual NVPairs) in Hashtable
         * Ex: xmlnames - NVPairs of xmlnames
         */
        private Hashtable _objectsTable = new Hashtable(); 

        private Hashtable _partnersTable = new Hashtable();
        
        private Properties cacheProperties = new Properties();
        
        public CachePropertiesLoader()
        {}
        
        /**
         *  Method loads the cache specific properties and converts them into a
         *  specific map form.
         *  @see FileInputStream
         */
        public void loadProperties() throws Exception
        {
            
            cachesDefRefreshFreq =
                Long.parseLong( (String)ConfigProps.getProperty("com.blnz.fxpl.CacheProxy.caches.default.refresh",
                                                                "24") );
            cachesDefMaxsize = 
                Integer.parseInt( (String)ConfigProps.getProperty("com.blnz.fxpl.CacheProxy.caches.default.maxsize",
                                                                  "10000") );
            String caches = 
                (String) ConfigProps.getProperty("com.blnz.fxpl.CacheProxy.caches"); 
            

            if ( caches == null ) {
                return;
            }
            
            Enumeration pe = ConfigProps.getProperties().propertyNames();
            while (pe.hasMoreElements()) {
                String key = (String) pe.nextElement();
                if (key.startsWith("palomar.CacheProxy.")) {
                    _cacheProperties.put(key, ConfigProps.getProperty(key));
                }
            }
            
            StringTokenizer cacheTokenizer = new StringTokenizer(caches, ",");
            while (cacheTokenizer.hasMoreElements()) {            // append to a list
                String nextCache = cacheTokenizer.nextToken();

                Properties nextPropTable = new Properties();
                for (Enumeration propsEnum = _cacheProperties.keys() ; 
                     propsEnum.hasMoreElements() ; ) {
                    String nextProp = (String) propsEnum.nextElement();
                    if ( nextProp.indexOf("." + nextCache + ".") != -1 ) {
                        nextPropTable.put(nextProp , 
                                          (String)ConfigProps.getProperty(nextProp));
                    }
                }
                _objectsTable.put(nextCache, nextPropTable);
            }

            String partners = 
                (String) ConfigProps.getProperty("com.blnz.fxpl.CacheProxy.partners"); 
            
            String excludedPartner = (String)ConfigProps.getProperty("com.blnz.fxpl.CacheProxy.excludePartner");    
            

            if ( partners == null ) {
                return;
            }
            
            StringTokenizer partnersTokenizer = new StringTokenizer(partners, ",");

            while (partnersTokenizer.hasMoreElements()) {            // append to a list
                String partner = partnersTokenizer.nextToken();
                if (!partner.equals(excludedPartner)) {
                    try {
                        PartnerNotifier pn = new PartnerNotifier(partner, ConfigProps.getProperties());
                        if (pn != null) {
                            _partnerNotifiers.add(pn);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }
        
        /** 
         * Method returns the generated properties map.
         */
        public Hashtable getCachesTable()
        {
            return _objectsTable;
        }

        /**
         *
         */
        public Hashtable getPartners()
        {

            return _partnersTable;
        }

    } 

    /**
     *
     */
    private class PartnerNotifier implements Runnable
    {
        private String _name = null;
        private String _url = null;
        private String _authType = "none";
        private Hashtable _caches = new Hashtable();
        private Thread _notifyThread = null;
        private long _lastRun = 0;

        public PartnerNotifier(String partnerName, Properties props)  throws Exception
        {
            _name = partnerName;

            String urlProp = "CacheProxy.partner." + _name + ".url";
            _url = props.getProperty(urlProp);

            if (_url == null) {
                throw new Exception("no url for CachePartner [" + _name + "]");
            }

            String authTypeProp = "CacheProxy.partner." + _name + ".authType";
            _authType = props.getProperty(authTypeProp, "none");

            if (_authType == null) {
                throw new Exception("no authType for CachePartner [" + _name + "]");
            }

            System.out.println("CacheProxy: setting partnerNotifier for {" + urlProp + "} at {" + _url + "}");

            String caches = props.getProperty("CacheProxy.partner." + _name + ".caches");
            if (caches != null) {
                StringTokenizer cachesTokenizer = new StringTokenizer(caches, ",");
                
                while (cachesTokenizer.hasMoreElements()) {            // append to a list
                    
                    String cache = cachesTokenizer.nextToken();
                    
                    System.out.println("adding {CacheProxy.partner." + _name + ".caches}=" + cache);
                    
                    _caches.put(cache, new Vector());
                }
            }
        }
        
        public boolean shouldNotify(String cacheName) 
        {
            return (_caches.get(cacheName) != null);
        }

        public synchronized void notify(String cacheName, String key) 
        {

            
            Vector cacheKeys = (Vector) _caches.get(cacheName);
            if (cacheKeys == null) {
                // not interested in this cache
                return;
            }

            if (!cacheKeys.contains(key)) {
                // FIXME: need to intern keys?
                // already pending 
                cacheKeys.add(key);
            }
            
            if (_notifyThread != null) {
                // we're already sending
                return;
            } else {
                // start up a background thread to send notification to partner
                _notifyThread = new Thread(this);
                _notifyThread.start();
            }
        }

        public void run()
        {

            while (true) {
                // avoid hammering partner by waiting at least 1 second between sends
                long now = new Date().getTime();
                if ((now - _lastRun) < 1000) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException ex) {
                        // no worries
                    }
                }
                
                String message = genMessage();
                if (message == null) {
                    // all done.  this way out ..
                    _lastRun = new Date().getTime();
                    _notifyThread = null;
                    return;
                } else {
                    sendMessage(message);
                }
            }
        }


        private String genMessage()
        {

            Enumeration en = _caches.keys();

            String message = "";
            while (en.hasMoreElements()) {
                String cacheName = (String) en.nextElement();
                Vector v = (Vector) _caches.get(cacheName);
                if (v.size() > 0) {
                    message += "<changes cacheName='" + cacheName + "'>";
                    Enumeration keys = v.elements();
                    while (keys.hasMoreElements()) {
                        String val = (String) keys.nextElement();
                        message += "<changed key='" + val + "'/>";
                        v.remove(val);
                    }
                    
                    message += "</changes>";
                }
            }
            if (message.length() > 0) {
                return "<cacheNotify>" + message + "</cacheNotify>";
            } 
            return null;
        }


        private void sendMessage(String message)
        {

            try {
                URL url = new URL(_url);

                HttpURLConnection c  = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");


                if ("basic".equals(_authType)) {
                    
                    
                    User admin = Security.getSecurityService().getAdminUser();
                   
                    c.setRequestProperty("Authorization", "Basic " +  
                                         Base64Encode.encode(admin.getUsername() +
                                                             ":" + admin.getPassword())); 
                    
                }

                c.setDoOutput(true);
            
                c.connect();
                
                OutputStreamWriter out = 
                    new OutputStreamWriter(c.getOutputStream(),
                                           "UTF8");
                PrintWriter pw = new PrintWriter(out);
                pw.println(message);
                pw.close();

                int responsecode = c.getResponseCode();
                
                switch (responsecode) {
                    // here valid codes!
                case HttpURLConnection.HTTP_OK :

                    break;
                case HttpURLConnection.HTTP_MOVED_PERM :
                case HttpURLConnection.HTTP_MOVED_TEMP :
                    break;
                case HttpURLConnection.HTTP_NOT_MODIFIED :
                    //not modified so do not download. just return
                    //instead and trace out something so the username
                    //doesn't think that the download happened when it
                    //didnt

                    c.disconnect();
                    return;
                default :
                    c.disconnect();
                    throw new Exception("Invalid HTTP response code: " + 
                                        responsecode + " from: " + _url);
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
