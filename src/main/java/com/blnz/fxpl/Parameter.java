package com.blnz.fxpl;

/**
 *
 */
public class Parameter 
{
    private String _name;
    private int _type;
    private String _value;

    /**
     * construct a Parameter of the given name and value
     * @param type is one ... err ... of the java SQL types?
     */
    public Parameter(String name, int type, String value) 
    {
        _name = name;
        _type = type;
        _value = value;
    }

    /**
     *
     */
    public String getName() 
    {
        return _name;
    }

    /**
     *
     */
    public int getType() 
    {
        return _type;
    }

    /**
     *
     */
    public String getValue() 
    {
        return _value;
    }
}
