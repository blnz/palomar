package com.blnz.fxpl.security.impl;
import com.blnz.fxpl.security.*;

/**
 * This is the Group interface of the Security package.
 * The interface may be used for getting group-related security information.
 * The interface may also be used to update the group information.
 */
public class FakeGroup implements Group
{
    /**
     * @return the group's integer ID value
     */
    public String getID()
    { return getName(); }

    /**
     * @return the group's name
     */
    public String getName()
    { return "Administrator"; }

    /**
     * @return all the users that belong to this group
     */
    public User[] getGroupUsers()
    { return null; }

    /**
     * set the name of the group
     * @param name the new name for the group
     * @return the old group name, or empty string if the 
     *                 group didn't hava a name,
     * or null if permission denied.
     */
    public void setName(String name) throws Exception
    {
        return;
    }

    /**
     * test if this group is the administrator group for a given resource
     * @return <code>true</code> if the group is the Administrator, 
     * <code>false</code> otherwise.
     */
    public boolean isAdministrator()
    {
        return true;
    }
}



