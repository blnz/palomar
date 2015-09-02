package com.blnz.fxpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Locator;
import com.blnz.fxpl.dom.Location;


/**
 * general mechanism for signalling errors during ECHO Processing
 */
public class FXException  extends Exception
{
    
    private Exception _why = null;
    private List _locations = new ArrayList();

    public FXException()
    {
        super();
    }

    /** 
     * create a new FXException
     */
    public FXException(String msg)
    {
	super(msg);
    }

    /**
     * create a new ECHOException wrapped over another Exception 
     * along with a  message 
     *
     * @param msg a human readable message
     * @param why an <code>Exception</code> value, the underlying cause
     */
    public FXException(String msg, Exception why) 
    {
	super(msg);
	_why = why;
    }

    /**
     * create a new ECHOException wrapped over another Exception
     * @param why an <code>Exception</code> value, the underlying cause.
     */
    public FXException(Exception why)
    {
        _why = why;
    }

    /**
     *  @return the embedded Exception, if there is one, otherwise null
     */
    public Exception getException()
    {
	return _why;
    }
      
    public void addLocation(Locator location, String tagName) {
        if (location!=null && !"".equals(location.getSystemId())) {
            FXLocation echoLocation = new FXLocation(location, tagName);
            _locations.add(echoLocation);
        }
    }
    
    public List getLocations() {
        return _locations;
    }
    
    public String getMessage()
    {
        FXLocation lastLoc = null;
        if (_locations.size()==0) {
            if (_why != null) {
                return _why.getMessage();
            } else {
                return super.getMessage();
            }
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append( (_why == null) ? super.getMessage() : _why.getMessage());
            buffer.append("\n");
            for (Iterator i= _locations.iterator(); i.hasNext(); ) {
                FXLocation location = (FXLocation)i.next();
                if (lastLoc == null || ! lastLoc.equals(location)) {
                    buffer.append("\t\t");
                    buffer.append("at " + location.getTagName() + 
                                  " in " + location.getSystemId() + 
                                  " line: " + location.getLineNumber() + 
                                  " col: " + location.getColumnNumber());
                    buffer.append("\n");
                }
                lastLoc = location;
            }
            return buffer.toString();
        }
    }


    private class FXLocation extends Location 
    {

        protected String tagName;

        public FXLocation(Locator where, String tagName) 
        {
            super(where);
            this.tagName = tagName == null ? "" : tagName;
        }

        public String getTagName() 
        {
            return this.tagName;
        }
        
        public boolean equals(Object loc) 
        {
            if (loc == null) {
                return false;
            }
            if (! (loc instanceof FXLocation) ) {
                return false;
            }
            FXLocation elo = (FXLocation) loc;
            return (systemId.equals(elo.systemId) &&
                    tagName.equals(elo.tagName) &&
                    lineNumber == elo.lineNumber &&
                    columnNumber == elo.columnNumber) ;
                    

        }
    }

}
