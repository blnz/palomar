package com.blnz.fxpl;

/**
 * The Home interface (static methods) for FX from which one can obtain
 *  the system's FXConnectionFactory and FX XProcessors.
 *
 * The name of the system's FXConnectionFactory implementing class 
 * is identified by the property
 * <code>com.blnz.fxpl.FXConnectionFactory</code>.  
 * If this property
 * is not given, the implementing class will default to
 * <code>com.blnz.fxpl.core.FXLocalHomeImpl</code>
 *
 * Programmers will typically interact w this class to obtain
 * an FXConnection which they will use to evaluate FX scripts.
<code>

            String serviceName = "local";
            String userName = " ... " ;
            String password = " ... " ;

            FXConnection  econn =
                  FXHome.getFXConnection(serviceName, 
                                             userName, 
                                             password);

            FXContext context = econn.getExtendedContext();
            context.add("myKey", "myValue");
            


</code>
 */

public class FXHome
{

    private static FXConnectionFactory _cFact = null;



    // nobody get's to use this
    private FXHome() {}

    /**
     * The FX namespace is <code>http://namespaces.xmlecho.org/echo</code>
     */
    public static final String NAMESPACE = 
        "http://namespaces.blnz.com/fxpl";

    /**
     * The FX connection session 
     *  namespace is <code>http://namespaces.xmlecho.org/echoSession</code>
     */
    public static final String SESSION_NAMESPACE = 
        "http://namespaces.blnz.com/fxpl-session";

    /**
     * gets an FXConnection implementation with a connection
     *  to the named processor from the configured FXConnectionFactory
     */
    public static final FXConnection getFXConnection(String serviceName,
                                                     String userName,
                                                     String userCredential)
        throws FXException
    {
        return getConnectionFactory().getFXConnection(serviceName,
                                                      userName,
                                                      userCredential);
    }
    
    /**
     * obtain the FX Processor, configured through
     * properties for the given name from the system's FXService
     */
    public static final XProcessor getXProcessor(String svcName)
    {
        return getConnectionFactory().getXProcessor(svcName);
    }
    
    /**
     * obtain the system's FXService implementation
     */
    public static final FXConnectionFactory getConnectionFactory()
    { 
        if (_cFact == null) { 
            init(); 
        } 
        return _cFact; 
    }
    
    //
    private static synchronized void init()
    {
        if (_cFact != null) {
            return; // another thread beat us
        }
        try {
            // FIXME : is a system property the right place to get this?
            
            String implName = "com.blnz.fxpl.core.FXConnectionFactoryImpl";
            try {
                implName =  System.getProperty("com.blnz.fxpl.FXConnectionFactory",
                                               implName);
            } catch (java.security.AccessControlException e) {
                System.out.println(e.getMessage());
            }
            _cFact = (FXConnectionFactory) Class.forName(implName).newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            // we just want to have a fallback implementation, and continue
            _cFact = new com.blnz.fxpl.core.FXConnectionFactoryImpl();
        }
    }
}

