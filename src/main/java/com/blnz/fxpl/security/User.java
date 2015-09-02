//  $Id: User.java 414 2005-08-15 21:51:05Z blindsey $

package com.blnz.fxpl.security;

/**
 *  <p>Exposes the set of actions Security services expects to perform on
 *   user information.</p>
 *  <p>Use this interface to obtain, add or update a user.</p>
 */
public interface User
{
    /**
     * @return the User ID
     */
    public String getID();

    /**
     * check if a given string matches the user's password
     * @param password the given string
     * @return <code>true</code> if the password matches, 
     * <code>false</code> otherwise.
     */
    public boolean checkPassword(String password);

    /**
     * @return the user name
     */
    public String getUsername();

    /**
     * @return the user's password ... unencrypted, for now?
     */
    public String getPassword();

    /**
     * @return <code>true</code> if the user is an Administrator,
     * <code>false</code> otherwise.
     */
    public boolean isAdministrator();

    /**
     * @return the primary group of this user
     */
    public Group getPrimaryGroup();

    /**
     * @return all the groups this user belongs to
     */
    public Group[] getGroups();

    /**
     * Set the user name
     * @param name the name to be set to this user
     * <code>null</code> if permission denied
     */
    public void setUsername(String name)
        throws Exception;
    
    /**
     * Set the user password
     * @param password the password to be set to this user
     */
    public void setPassword(String password)
        throws Exception;

    /**
     * Retrieve an arbitrary property from the user.
     * @param name the name (key) for the property
     * @return the named property's string value
     */
    public String getProperty(String name);
    
    /**
     * Store an arbitrary property against the user.
     * @param name the name (key) for the property
     * @param value the named property's string value
     */
    public void setProperty(String name, String value);
}
