package com.blnz.fxpl.core;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StringReader;

import java.net.URL;

import java.util.Hashtable;

import com.blnz.fxpl.log.Logger;

import com.blnz.fxpl.FXConnection;
import com.blnz.fxpl.FXConnectionFactory;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;
import com.blnz.fxpl.XProcessor;

import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.xform.TransformService;
import com.blnz.fxpl.log.Log;

import com.blnz.xsl.om.NodeExtensionFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Simple, Java API to the FX Processor.
 */
public class FXConnectionFactoryImpl 
    implements FXConnectionFactory
{
    // a cache of named service implementations
    // FIXME: make this a LRU cache
    private Hashtable<String, FXConnection> _svcs = new Hashtable<String, FXConnection>();

    private Logger _logger = null;

    private NodeExtensionFactory _localCommandFact = null;
    private NodeExtensionFactory _localResponseFact = null;

    public FXConnectionFactoryImpl()
    {
        _logger = Log.getLogger();
    }

    /**
     * gets an FXConnection implementation with a connection
     *  to the named processor on behalf of the given user
     */
    public FXConnection getFXConnection(String serviceName,
                                                          String userName,
                                                          String userCredential)
        throws FXException
    { 
        
        String key = "{" + serviceName + "}{" + userName + "}";
        FXConnection svc = (FXConnection) _svcs.get(key);

        if (svc == null) {
            svc = new FXConnectionImpl(getXProcessor(serviceName), 
                                         userName, userCredential);
            _svcs.put(key, svc);
        }
        return svc;
    }
    
    /**
     * obtain an FX Processor
     */
    public XProcessor getXProcessor(String svcName)
    {
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("FXConnectionFactory::getXProcessor(\"" + 
                          svcName + "\")");
        }
        if (_localCommandFact == null) {
            buildCommandFactories();
        }
        
        String classname =
            ConfigProps.getProperty("com.blnz.fxpl.echo.FXProcessor." +
                                    svcName);
        
        XProcessorImpl xp = null ;
        
        if (classname != null) {
            try {
                Class c = Class.forName(classname);
                xp = (XProcessorImpl) c.newInstance();
            } catch (Exception ex) {
                Log.getLogger().warn("cannot load FXProcessor: " + 
                                            classname, ex);
            }
        }
        
        if (xp == null) {
            if ("http".equals(svcName)) {
                // xp = new XProcessorHTTPClientImpl();
            } else if ("proxy".equals(svcName)) {
                // xp = new XProcessorHTTPClientImpl();
                // xp.setURL(ConfigProps.getProperty("org.xmlecho.proxyHost"));
            } else if (svcName.indexOf("http://") == 0) {
                // xp = new XProcessorHTTPClientImpl();
                // xp.setURL(svcName);
            } else { 
                xp = new XProcessorImpl();
            }
        }
        
        xp.setRequestFactory((SimpleElementFactory)_localCommandFact);
        xp.setResponseFactory((SimpleElementFactory)_localResponseFact);

        return xp;
    }

    /**
     * create an initial context for a request
     */
    public FXContext createContext()
    {
        return new FXContextImpl();
    }

    /**
     *
     */
    private synchronized void buildCommandFactories()
    {

        if (_localCommandFact != null) {
            return;
        }
        String defaultTagMap = 
            "<bindings><binding tag='*Element' class='com.blnz.fxpl.core.FXRequestImpl' /></bindings>";

        String defaultResponseMap = 
            "<bindings><binding tag='*Element' class='com.blnz.fxpl.core.FXResponseImpl' /></bindings>";


        String localTagMap = 
            ConfigProps.getProperty("com.blnz.fxpl.localTagMap");

        if (true || localTagMap == null) {
            localTagMap = "./FXTagMap.xml";
        }
        
        _localCommandFact = new FXElementFactory();
        _localResponseFact = new FXElementFactory();

        URL coreTagMapURL = ConfigProps.getResource("/FXTagMap.xml", null);

        try {
            buildFactory((FXElementFactory)_localCommandFact, coreTagMapURL, defaultTagMap);
            buildFactory((FXElementFactory)_localResponseFact, coreTagMapURL, defaultResponseMap);
            //            System.out.println("FXConnectionFactoryImpl:: built 2 factories 1");
        } catch (RuntimeException ex) {
            Log.getLogger().warn("failed to load core TagMap: " + coreTagMapURL + " -- " + ex.getMessage());
        }
        
        URL tagMapURL = ConfigProps.getResource(localTagMap, ConfigProps.propsBaseURL());
        if (! coreTagMapURL.equals(tagMapURL)) {
            try {
                buildFactory((FXElementFactory)_localCommandFact, tagMapURL, defaultTagMap);
                buildFactory((FXElementFactory)_localResponseFact, tagMapURL, defaultResponseMap);
//                System.out.println("FXConnectionFactoryImpl:: built 2 factories 2");
            } catch (RuntimeException ex) {
                Log.getLogger().warn("failed to load secondary TagMap " + tagMapURL + " -- " + ex.getMessage());
            }
        }        
    }

    private NodeExtensionFactory buildFactory(FXElementFactory fact, URL mapURL, 
                                              String defaultTagMap)
    {
        
        TransformService tsh = XForm.getTransformService();
        
        if (mapURL == null) {
            System.out.println("buildFactory(): no tagmap URL");
            return null;
        }

        try {
            XMLReader r = tsh.createInputSourceReader();
            
            InputSource s = new InputSource(mapURL.toString());
            
            r.setContentHandler(fact);
            r.parse(s);
//            System.out.println("FXConnectionFactoryImpl:: built factory 2");
            return fact;
        } catch (Exception ex) {
//            System.out.println("EchoConnectionFactoryImpl: failed to initialize tag map");
            ex.printStackTrace();
            // FIXME: do something   
        }
        return null;
    }
    
}
