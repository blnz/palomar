package com.blnz.fxpl.security;

/**
 * This is the Group interface of the Security package.
 * The interface may be used for getting group-related security information.
 * The interface may also be used to update the group information.
 */
public interface Group
{
    /**
     * @return the group's integer ID value
     */
    public String getID();

    /**
     * @return the group's name
     */
    public String getName();

    /**
     * @return all the Users that belong to this group
     */
    public User[] getGroupUsers();

    /**
     * @param name the new name for the group
     */
    public void setName(String name) throws Exception;

    /**
     * test if this group is the administrator group for a given resource
     * @return <code>true</code> if the group is the Administrator, 
     * <code>false</code> otherwise.
     */
    public boolean isAdministrator();
}



