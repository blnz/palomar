package com.blnz.fxpl.security.impl;
import com.blnz.fxpl.security.*;

/**
 *  <p>Exposes the set of actions Security services expects to perform on
 *   user information.</p>
 *  <p>Use this interface to obtain, add or update a user.</p>
 */
public class FakeUser implements User
{
    /**
     * @return the User ID
     */
    public String getID()
    { return getUsername(); }

    /**
     * check if a given string matches the user's password
     * @param password the given string
     * @return <code>true</code> if the password matches, 
     * <code>false</code> otherwise.
     */
    public boolean checkPassword(String password)
    { return true; }

    /**
     * @return the user name
     */
    public String getUsername()
    { return "admin"; }

    /**
     * @return the user's password ... unencrypted, for now?
     */
    public String getPassword()
    { return "admin"; }

    /**
     * @return <code>true</code> if the user is an Administrator,
     * <code>false</code> otherwise.
     */
    public boolean isAdministrator()
    { return true; }

    /**
     * @return the primary group of this user
     */
    public Group getPrimaryGroup()
    { return null; }

    /**
     * @return all the groups this user belongs to
     */
    public Group[] getGroups()
    { return null; }

    /**
     * Set the user name
     * @param name the name to be set to this user
     * <code>null</code> if permission denied
     */
    public void setUsername(String name)
        throws Exception
    { return ; }
    
    /**
     * Set the user password
     * @param password the password to be set to this user
     * if permission denied
     */
    public void setPassword(String password)
        throws Exception
    { return ; }

    public String getProperty(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProperty(String name, String value) {
        // TODO Auto-generated method stub
    }
            
}
