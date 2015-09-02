package com.blnz.fxpl.xform;

/**
 * Super class for all Transformation Exceptions.
 */
public class TransformException extends Exception
{
    private int _errorId;
    private Exception _orgException = null;
    
    public TransformException()
    {
	super();
    }
    
    public TransformException(String errorMessage)
    {
	super(errorMessage);
    }
    
    public TransformException(String errorMessage, Exception orgEx)
    {
	this(errorMessage);
	_orgException = orgEx;
    }
    
    public TransformException(Exception orgEx)
    {
	this("whoops!");
	_orgException = orgEx;
    }
    
    // prefered constructor
    public TransformException(int errorId, String errorMessage)
    {
	this(errorMessage);
	this._errorId = errorId;
    }
    
    public String toString()
    {
	//FIXME: more meaningful message with error id's
	return _orgException == null ? super.getMessage(): super.getMessage()
            + "Details: " + _orgException.getMessage();
    }
    
    public String getMessage()
    {
	return toString();
    }

}
