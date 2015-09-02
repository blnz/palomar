package com.blnz.xsl.tr;

import com.blnz.xsl.om.Name;
import com.blnz.xsl.om.NodeExtensionFactory;

/**
 * maintains a list of a few options for how a XMLProcessor constructs
 * an object model
 */
public interface LoadContext
{
    /**
     * white space handling
     */
    public boolean getStripSource(Name elementTypeName);

    /**
     * include comment nodes?
     */
    public boolean getIncludeComments();

    /**
     * include processing instructions?
     */
    public boolean getIncludeProcessingInstructions();

    /**
     * are we instrumenting this transformer?
     */
    public ActionDebugTarget getDebugger();

    /**
     *
     */
    public NodeExtensionFactory getExtensionFactory();
}
