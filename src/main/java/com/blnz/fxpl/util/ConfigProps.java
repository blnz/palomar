package com.blnz.fxpl.util;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class ConfigProps
{
    private static Properties _props = new Properties();

    private static boolean _loaded = false;
    private static URL _propsURL = null;
    private static Files _files = null;

    private static void init()
    {

        String propsFile = "/fxpl.properties";
        try {
            propsFile = System.getProperty("fxpl.config.dir", propsFile);
        } catch (java.security.AccessControlException e) {
            System.out.println("ConfigProps: Initialization error: " + e.getMessage());
        }


        System.out.println("ConfigProps: init() -- propsFile = " + propsFile);
        _propsURL = getResource(propsFile, null);

        InputStream is = null;
        if (_propsURL != null ) {

            System.out.println("ConfigProps url is {" + _propsURL.toString() + "}");
            try {
                is = _propsURL.openStream();
            } catch (Exception ex) {
                System.out.println("ConfigProps: couldn't open stream");
                ex.printStackTrace();
            }
        } else {
            System.out.println("null propsURL");
        }
        if (is != null) {
            try {
                load(is);
                return;
            } catch (Exception ex) {
                
                System.out.println("failed to load properties");
                ex.printStackTrace();

            }
        }

        System.out.println("ConfigProps: cannot load properties, but not bailing");
        // System.exit(1);
    }

    /**
     * @return the URL from which we read the base config properties
     */
    public static URL propsBaseURL()
    {
        if (! _loaded) {
            init();
        }
        return _propsURL;
    }


    /**
     *
     */
    public static String getProperty(String key)
    {
        if (! _loaded) {
            init();
        }
        return _props.getProperty(key);
    }
    
    /**
     *
     */
    public static String getProperty(String key, String defaultValue)
    {
        if (! _loaded) {
            init();
        }
        return _props.getProperty(key, defaultValue);
    }
    
    /**
     *
     */
    public static void setProperty(String key, String value)
    {
        if (! _loaded) {
            init();
        }
        _props.setProperty(key, value);
    }
    
    /**
     *
     */
    public static void load(InputStream inStream) throws IOException
    {
        _props.load(inStream);
        _loaded = true;
    }
    
    /**
     *
     */
    public static Properties getProperties()
    {
        if (! _loaded) {
            init();
        }
        return _props;
    }

    /**
     *
     */
    public static URL getResource(String filename, URL baseURL)
    {
        URL url = null;
        // first, see if it's a url
        try {
            if (baseURL != null) {
                filename = new URL(baseURL, filename).toString();
//                System.out.println("configProps::URL from baseURL + filename" + filename);
            }
            
            url = new URL(filename);
        } catch (Exception ex) {
            //            ex.printStackTrace();
        }
        
        // see if our class loader can find it
        if (url == null ) {
            url = (new ConfigProps()).getClass().getResource(filename);
//            System.out.println("configProps::URL getclass + filename" + url.toString());
        } 

        return url;
    }

    /**
    *
    */
   public static Files getFiles()
   {
       if (! _loaded) {
           init();
       }
       if (_files == null) {
           _files = new Files();
       }
       return _files;
   }
}

