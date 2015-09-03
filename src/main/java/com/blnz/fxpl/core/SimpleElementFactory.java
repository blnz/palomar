package com.blnz.fxpl.core;

import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.NodeExtension;
import com.blnz.xsl.om.NodeExtensionFactory;

import com.blnz.xsl.tr.LoadContext;
import com.blnz.xsl.tr.ActionDebugTarget;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;



/**
 * This is a convenience class for creating application-specific elements
 * associated with specified (or default) XML namespaces.  It maintains
 * tables mapping element tag names to classes, and uses them as needed
 * to instantiate classes.  The string <em>*Element</em>, which is not a
 * legal XML element name, may be used to map otherwise unrecognized tags
 * to a particular class.  If this factory is not configured, then all
 * mappings are to the <a href=ElementNode.html>ElementNode</a> class.
 * Erroneous mappings are fatal errors.
 *
 * <P> A suggested XML syntax for recording these bindings, which may
 * in the future be explicitly supported, is: <PRE>
 * <b>&lt;bindings xmlns="..."&gt;</b>
 *     &lt;!-- first, bindings for the "default" namespace --&gt;
 *     <b>&lt;binding tag="..." class="..."/&gt;</b>
 *     &lt;binding <b>tag="*Element"</b> class="..."/&gt;
 *     ...
 *
 *     &lt;!-- then bindings for other namespaces --&gt;
 *     <b>&lt;namespace uri="..."&gt;</b>
 *         &lt;binding tag="..." class="..."/&gt;
 *         ...
 *     <b>&lt;/namespace&gt;</b>
 *
 *     &lt;!-- can specify JAR files for namespaces --&gt;
 *     &lt;namespace uri="..." <b>jar="..."</b>&gt;
 *         &lt;binding tag="..." class="..."/&gt;
 *         ...
 *     &lt;/namespace&gt;
 *     ...
 * <b>&lt;/bindings&gt;</b>
 * </PRE>
 *
 * <P> Note that while most URIs used to identify namespaces will be URLs,
 * such as <em>http://www.example.com/xml/purchasing</em>, some may also
 * be URNs like <em>urn:uuid:221ffe10-ae3c-11d1-b66c-00805f8a2676</em>.
 * You can't assume that the URIs are associated with web-accessible data;
 * they must be treated as being no more than distinguishable strings.
 *
 * <P> Applications classes configuring an element factory will need to
 * provide their own class loader (<code>this.class.getClassLoader</code>)
 * to get the desired behavior in common cases.  Classes loaded via some
 * URL will similarly need to use a network class loader.
 *
 */
public class SimpleElementFactory implements NodeExtensionFactory, LoadContext
{
    // in the absense of a mapping tied to namespace URI, use these
    private Dictionary		defaultMapping;
    private ClassLoader		defaultLoader;

    private String		defaultNs;

    // these hold mappings tied to namespace URIs
    private Dictionary		nsMappings;
    private Dictionary		nsLoaders;
    private Locale 		locale = Locale.getDefault ();


    /**
     * Constructs an unconfigured element factory.
     */
    public SimpleElementFactory () { }

    /**
     * Records a default element name to namespace mapping, for use
     * by namespace-unaware DOM construction and when a specific
     * namespace mapping is not available.
     *
     * @param dict Keys are element names, and values are either class
     *	names (interpreted with respect to <em>loader</em>) or class
     *	objects.  This value may not be null, and the dictionary is
     *	retained and modified by the factory.
     * @param loader If non-null, this is used instead of the bootstrap
     *	class loader when mapping from class names to class objects.
     */
    public void addMapping (Dictionary dict, ClassLoader loader)
    {
	if (dict == null)
	    throw new IllegalArgumentException ();

	defaultMapping = dict;
	defaultLoader = loader;
    }

    /**
     * Records a namespace-specific mapping between element names and
     * classes.
     *
     * @param namespace A URI identifying the namespace for which the
     *	mapping is defined
     * @param dict Keys are element names, and values are either class
     *	names (interpreted with respect to <em>loader</em>) or class
     *	objects.  This value may not be null, and the dictionary is
     *	retained and modified by the factory.
     * @param loader If non-null, this is used instead of the bootstrap
     *	class loader when mapping from class names to class objects.
     */
    public void addMapping (
	String		namespace,
	Dictionary	dict,
	ClassLoader	loader) 
    {
        //        System.out.println("Adding mappings for NS: {" + namespace + "}");
	if (namespace == null || dict == null)
	    throw new IllegalArgumentException ();
	if (nsMappings == null) {
	    nsMappings = new Hashtable ();
	    nsLoaders = new Hashtable ();
	}
        if (nsMappings.get(namespace) != null) {
            throw new RuntimeException("Namespace already defined: " + namespace);
        }
	nsMappings.put (namespace, dict);
	if (loader != null) {
	    nsLoaders.put (namespace, loader);
        }
    }
    
    /**
     * Defines a URI to be treated as the "default" namespace.  This
     * is used only when choosing element classes, and may not be
     * visible when instances are asked for their namespaces. 
     */
    public void setDefaultNamespace (String ns)
    { defaultNs = ns; }
    
    private Class map2Class (String key,
                             Dictionary	node2class,
                             ClassLoader loader)
    {
        System.out.println("SimpleElementFactory::map2Class Entry " + key);
//
//        if (true) {
//          Enumeration keys = node2class.keys(); 
//          while ( keys.hasMoreElements() ) {
//              System.out.println( " key: " + keys.nextElement().toString());
//          }
//
//        }
	Object		mapResult = node2class.get (key);

	if (mapResult == null) {
            //            System.out.println("SimpleElementFactory::map2Class " + key);
	    return null;
        }        
	if (mapResult instanceof Class) {
	    return (Class) mapResult;
        }

	if (mapResult instanceof String) {
	    String	className = (String) mapResult;
	    Class	retval = null;

            System.out.println("SimpleElementFactory::map2Class haveString for '" + key + "' of '" + className);
	    try {
		if (loader == null)  {
		    retval = Class.forName (className);
		} else {
		    retval = loader.loadClass (className);
		}
		node2class.put (key, retval);
		return retval;
		
	    } catch (ClassNotFoundException e) {
	        System.out.println("SimpleElementFactory::map2Class ClassNotFound " + key);
	        return null;
                //	throw new IllegalArgumentException (getMessage ("SEF-001",
		//	new Object [] { key, className, e.getMessage ()}));
	    } catch (Throwable ex) {
	        ex.printStackTrace();
	    } finally {
//	        System.out.println("whoops");
	    }
	}

	// another option:  clone elements, resetting parent
	// and document associations?

	throw new IllegalArgumentException (getMessage ("SEF-002", 
				new Object [] { key }));
    }

    private NodeExtension doMap (String		tagName,
                                 Dictionary	node2class,
                                 ClassLoader	loader
                                 ) 
    {
        Class theClass;
	NodeExtension retval = null;
        
	theClass = map2Class (tagName, node2class, loader);
	if (theClass == null) {
	    theClass = map2Class ("*Element", node2class, loader);
        }
	if (theClass == null) {
	    // retval = new FXRequestImpl(tagName);
	} else {
	    try {
		retval = (NodeExtension) theClass.newInstance ();
	    } catch (Exception e) {
                //InstantiationException
                //IllegalAccessException
		throw new IllegalArgumentException (getMessage ("SEF-003",
                                                                new Object [] {tagName, 
                                                                               theClass.getName (),
                                                                               e.getMessage () }));
	    }
	}
	return retval;
    }

    /**
     * Creates a NodeExtension by using the mapping associated with the
     * specified namespace, or the default namespace as appropriate.
     * If no mapping associated with that namespace is defined, then
     * the default mapping is used.
     *
     * @param namespace URI for namespace; null indicates use of
     *	the default namespace, if any.
     * @param localName element tag, without any embedded colon
     */
    public NodeExtension createElementEx (String namespace, String localName)
    {
	Dictionary	mapping = null;

        NodeExtension ex;

        //          System.out.println("Getting class for {" + localName + "} in {" +
        //                             namespace);
        
	if (namespace == null) {
	    namespace = defaultNs;
        }
        
	if (nsMappings != null)
	    mapping = (Dictionary) nsMappings.get (namespace);
	if (mapping == null) {
	    ex = doMap(localName, defaultMapping, defaultLoader);
	} else {
	    ex = doMap(localName, mapping,
                       (ClassLoader)nsLoaders.get (namespace));
        }
        //        System.out.println("returning: " + ex.getClass().getName());
        return ex;
    }

    /**
     * Creates an element by using the default mapping.
     *
     * @param tag element tag
     */
    public NodeExtension createElementEx (String tag)
    {
	return doMap (tag, defaultMapping, defaultLoader);
    }

    /*
     * Gets the messages from the resource bundles for the given messageId.
     */
    String getMessage (String messageId) 
    {
   	return getMessage (messageId, null);
    }

    /*
     * Gets the messages from the resource bundles for the given messageId
     * after formatting it with the parameters passed to it.
     */
    //XXX use the default locale only at this point.
    String getMessage (String messageId, Object[] parameters) 
    {
        return messageId;
    }

    /**
     * white space handling
     */
    public boolean getStripSource(Name elementTypeName)
    {
        return false;
    }

    /**
     * include comment nodes?
     */
    public boolean getIncludeComments()
    {
        return true;
    }


    /**
     * include processing instructions?
     */
    public boolean getIncludeProcessingInstructions()
    {
        return true;
    }

    /**
     * are we instrumenting this transformer?
     */
    public ActionDebugTarget getDebugger()
    {
        return null;
    }

   /**
     *
     */
    public NodeExtensionFactory getExtensionFactory()
    {
        return this;
    }

}
