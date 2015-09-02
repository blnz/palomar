package com.blnz.fxpl.security.impl;

import com.blnz.fxpl.security.SecurityService;
import com.blnz.fxpl.security.SecurityServiceException;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.security.Group;
import com.blnz.fxpl.security.PermissionSet;

import java.util.BitSet;

/**
 *  implements the SecurityService Interface
 */
public class SecurityServiceImpl implements SecurityService
{
    private static final String _serviceName = "Security";
    private static SecurityService _securityService = null;

    /**
     * The following constants specify permission types, one of these
     * constants should be used when calling the <code>checkPermission()</code>
     * method.
     */
    protected static int OWNER_READ = 0;
    protected static int OWNER_WRITE = 1;
    protected static int OWNER_EXECUTE = 2;
    protected static int GROUP_READ = 4;
    protected static int GROUP_WRITE = 5;
    protected static int GROUP_EXECUTE = 6;
    protected static int WORLD_READ = 8;
    protected static int WORLD_WRITE = 9;
    protected static int WORLD_EXECUTE = 10;

    /**
     *
     */
    public SecurityServiceImpl()
    {

    }

    /**
     * check the validity of the username and password.
     * @param userName username
     * @param password password
     * @return a <code>User</code> object if the user is validated, 
     * <code>null</code> if the user is invalid.
     */
    public User login(String userName, String password)
        throws SecurityServiceException
    {
        return null;
    }

    /**
     * check the validity of a hash code.
     * @param hash the hash code
     * @return a <code>User</code> object if the user is validated, 
     * <code>null</code> if the user is invalid.
     */
    public User login(String hash)
        throws SecurityServiceException
    {
        return null;
    }

    /**
     *Check if a given user is an Administrator
     *@param userId the ID of the User
     *@return <code>true</code> if the user is an Administrator,
     * <code>false</code> if the user is not an Administrator
     */
    public boolean isAdministrativeUser(String userId)
    {
            return false;
    }

    /**
     *check if a user has a certain permission.
     *@param user a User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the
     *  granted permission information to be checked against.
     *@param perm a String indicating the permission to be checked. The 
     *       valid value (case insensitive) are "read", "write", "execute".
     *@return <code>true</code> if the <code>perm</code> is granted to
     *          the <code>user</code>,
     *<code>false</code> if the <code>perm</code> is not granted to
     *       the <code>user</code>.
     */
    public boolean checkPermission(User user, PermissionSet mask, 
                                   String perm)
    {
        if (perm.equalsIgnoreCase(PERMISSION_READ)) {
            return checkRead(user, mask);
        } else if (perm.equalsIgnoreCase(PERMISSION_WRITE)) {
            return checkWrite(user, mask);
        } else if (perm.equalsIgnoreCase(PERMISSION_EXECUTE)) {
            return checkExecute(user, mask);
        }
        return false;
    }

    /**
     *check if a user has a certain permission.
     *@param userId the ID of the user whose permission needs to
     *  be checked.
     *@param mask a PermissionSet object containing all the
     *          granted permission information to be checked against.
     *@param perm a String indicating the permission to be checked.
     *    The valid value (case insensitive) are "read", "write", "execute".
     *@return <code>true</code> if the <code>perm</code> is granted
     *   to the <code>userId</code>,
     * <code>false</code> if the <code>perm</code> is not granted to
     *       the <code>userId</code>.
     */
    public boolean checkPermission(String userId, PermissionSet mask, String perm)
    {
        if (perm.equalsIgnoreCase(PERMISSION_READ)) {
            return checkRead(userId, mask);
        }  else if (perm.equalsIgnoreCase(PERMISSION_WRITE)) {
            return checkWrite(userId, mask);
        } else if (perm.equalsIgnoreCase(PERMISSION_EXECUTE)) {
            return checkExecute(userId, mask);
        }
        return false;
        
    }

    /**
     *check if a user has a "read" permission.
     *@param userId the ID of the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the
     *  granted permission information to be checked against.
     *@return <code>true</code> if the "read" permission is 
     * granted to the <code>userId</code>,
     *<code>false</code> if the "read" permission is not
     *  granted to the <code>userId</code>.
     */
    public boolean checkRead(String userId, PermissionSet mask)
    {
        try {
            User user = getUser(userId);
            return checkRead(user, mask);
        } catch (Exception e) {
            return false;
        }
    }
            
  
    /**
     *check if a user has a "write" permission.
     *@param userId the ID of the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the
     * granted permission information to be checked against.
     *@return <code>true</code> if the "write" permission 
     * is granted to the <code>userId</code>,
     *<code>false</code> if the "write" permission is not
     *  granted to the <code>userId</code>.
     */     
    public boolean checkWrite(String userId, PermissionSet mask)
    {

        try {
            User user = getUser(userId);
            return checkWrite(user, mask);
        } catch (Exception e) {
            return false;
        }
 
    }
            
    /**
     *check if a user has an "execute" permission.
     *@param userId the ID of the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the
     *  granted permission information to be checked against.
     *@return <code>true</code> if the "execute" permission is
     *  granted to the <code>userId</code>,
     *<code>false</code> if the read "permission" is not granted
     *  to the <code>userId</code>.
     */
    public boolean checkExecute(String userId, PermissionSet mask)
    {
        try {
            User user = getUser(userId);
            return checkExecute(user, mask);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *check if a user has a "read" permission.
     *@param user the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the 
     *  granted permission information to be checked against.
     *@return <code>true</code> if the "read" permission is 
     * granted to the <code>user</code>,
     *<code>false</code> if the "read" permission is not granted 
     * to the <code>user</code>.
     */          
    public boolean checkRead(User user, PermissionSet mask)
    {

        BitSet bits = mask.getPermissions();
        if (bits.get(WORLD_READ)){
            return true;
        }	
        
        Group[] groups = user.getGroups();
        if (groups != null) {
            for (int i = 0; i < groups.length; i++){
                if (groups[i].isAdministrator() || 
                    (groups[i].getID().equals(mask.getOwningGroupID()) && bits.get(GROUP_READ))){
                    return true;
                }
            }	
        }
        
        if (bits.get(OWNER_READ)) {
            if (user.getID().equals( mask.getOwnerID() )) {
                return true;
            }
        }
        return false;
    }

    /**
     *check if a user has a "write" permission.
     *@param user the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *  permission information to be checked against.
     *@return <code>true</code> if the "write" permission is granted
     *  to the <code>user</code>,
     *<code>false</code> if the "write" permission is not granted to
     *  the <code>user</code>.
     */          
    public boolean checkWrite(User user, PermissionSet mask)
    {
        BitSet bits = mask.getPermissions();
        if (bits.get(WORLD_WRITE)) {
            return true;
        }

        Group[] groups = user.getGroups();
        if (groups != null) {
            for (int i = 0; i < groups.length; i++)
                if (groups[i].isAdministrator() || 
                    (groups[i].getID().equals(mask.getOwningGroupID())  && bits.get(GROUP_WRITE)))
                    return true;
        }
        
        if (bits.get(OWNER_WRITE)) {
            if (user.getID().equals(mask.getOwnerID())) {
                return true;
            }
        }
        return false;
    }

    /**
     *check if a user has a "execute" permission.
     *@param user the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all
     *  the granted permission information to be checked against.
     *@return <code>true</code> if the "execute" permission is 
     * granted to the <code>user</code>,
     *<code>false</code> if the "execute" permission is not granted
     *   to the <code>user</code>.
     */          
    public boolean checkExecute(User user, PermissionSet mask)
    {

        BitSet bits = mask.getPermissions();
        if (bits.get(WORLD_EXECUTE)) return true;
 
        Group[] groups = user.getGroups();
        if (groups != null) {
            for (int i = 0; i < groups.length; i++) {
                if (groups[i].isAdministrator() || 
                    (groups[i].getID().equals(mask.getOwningGroupID()) && bits.get(GROUP_EXECUTE))) {
                    return true;
                }
            }
        }
        
        if (bits.get(OWNER_EXECUTE)) {
            if (user.getID().equals(mask.getOwnerID())) return true;
        }
        return false;

    }

    /**
     *set the default granted permission.
     *The default is Owner "read", "write", "execute"; 
     * Group "read" and World "read".
     *@param mask the PermissionSet object to be set to default values.
     */
    public void setDefault(PermissionSet mask)
    {
        BitSet clearBits = new BitSet(32);
        BitSet bits = mask.getPermissions();
        bits.and(clearBits);
        bits.set(OWNER_READ);
        bits.set(OWNER_WRITE);
        bits.set(OWNER_EXECUTE);
        bits.set(GROUP_READ);
        bits.set(WORLD_READ);
    }

    /**
     * set a Owner "Read" permission
     *@param mask the PermissionSet object to be set
     *  with Owner "Read" permission
     */
    public void setOwnerRead(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(OWNER_READ);
        } else {
            bits.clear(OWNER_READ);
        }
    }

    /**
     *set a Owner "Write" permission
     *@param mask the PermissionSet object to be set with
     * Owner "Write" permission
     */
    public void setOwnerWrite(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(OWNER_WRITE);
        } else {
            bits.clear(OWNER_WRITE);
        }
    }

    /**
     *set a Owner "Execute" permission
     *@param mask the PermissionSet object to be set 
     * with Owner "Execute" permission
     */
    public void setOwnerExecute(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(OWNER_EXECUTE);
        } else {
            bits.clear(OWNER_EXECUTE);
        }
    }

    /**
     *set a Group "Read" permission
     *@param mask the PermissionSet object to be set 
     * with Group "Read" permission
     */
    public void setGroupRead(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(GROUP_READ);
        } else {
            bits.clear(GROUP_READ);
        }
    }

    /**
     *set a Group "Write" permission
     *@param mask the PermissionSet object to be set 
     *  with Group "Write" permission
     */
    public void setGroupWrite(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(GROUP_WRITE);
        } else {
            bits.clear(GROUP_WRITE);
        }
    }

    /**
     *set a Group "Execute" permission
     *@param mask the PermissionSet object to be set
     *  with Group "Execute" permission
     */
    public void setGroupExecute(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(GROUP_EXECUTE);
        } else {
            bits.clear(GROUP_EXECUTE);
        }
    }

    /**
     *set a World "Read" permission
     *@param mask the PermissionSet object to be set with
     *  World "Read" permission
     */
    public void setWorldRead(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(WORLD_READ);
        } else {
            bits.clear(WORLD_READ);
        }
    }

    /**
     *set a World "Write" permission
     *@param mask the PermissionSet object to be set with
     *  World "Write" permission
     */
    public void setWorldWrite(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(WORLD_WRITE);
        } else {
            bits.clear(WORLD_WRITE);
        }
    }

    /**
     *set a World "Execute" permission
     *@param mask the PermissionSet object to be set with
     *  World "Execute" permission
     */
    public void setWorldExecute(PermissionSet mask, boolean bit)
    {
        BitSet bits = mask.getPermissions();
        if (bit) {
            bits.set(WORLD_EXECUTE);
        } else {
            bits.clear(WORLD_EXECUTE);
        }
    }

    /**
     * Add a new user to the database. Only the administrator 
     *  is allowed to do so.
     *@param userName the new User Name to be added to the database.
     *@param password the new User password to be added to the database.
     *@param callerId the userId for the user who calls this method.
     *@return <code>User</code> object if the <code>user</code> is
     *  added successfully,
     <code>null</code> if permission is denied or addition is unsuccessful.
    */
    public User addUser(String userName, String password, String callerId)
    {
        return null;
    }

    /**
     *Update a user's information. Only the administrator or
     *  the very user himself are allowed to do so.
     *@param user the User object containing the new user 
     * information to be updated in the database.
     *@param callerId the userId for the user who calls this method.
     *@return <code>true</code> if the <code>user</code> is updated 
     * successfully,
     *  <code>false</code> if permission is denied or update is unsuccessful.
     */
    public boolean updateUser(User user, String callerId)
    {
        return false;
    }

    /**
     *Delete a user's entry from the database. Only the administrator
     *  is allowed to do so.
     *@param user the new User object to be deleted from the database.
     *@param callerId the userId for the user who calls this method.
     *@return <code>true</code> if the <code>user</code> is deleted 
     * successfully,
     * <code>false</code> if permission is denied or deletion is unsuccessful.
     */
    
    public boolean deleteUser(User user, String callerId)
    {
        return false;
    }


    /**
     *Add a new Group to the database. Only the administrator is
     *  allowed to do so.
     *@param groupName The new Group name to be added.
     *@param callerId the userId of the user who calls this method.
     *@return <code>Group</code> object if the <code>group</code> is added
     *   successfully,
     * <code>null</code> if permission is denied or addition is unsuccessful.
     */
    public Group addGroup(String groupName, String callerId)
    {
        return null;
    }

    /**
     *Update a Group's information in the database. 
     * Only the administrator is allowed to do so.
     *@param group The Group object containing the updated information.
     *@param callerId the userId of the user who calls this method.
     *@return <code>true</code> if the <code>group</code> is updated 
     * successfully,
     <code>false</code> if permission is denied or update is unsuccessful.
    */
    public boolean updateGroup(Group group, String callerId)
    {
        return false;
    }

    /**
     *Delete a group from the database. Only the administrator is allowed to do so.
     *@param group The Group object to be deleted from the database.
     *@param callerId the userId of the user who calls this method.
     *@return <code>true</code> if the <code>group</code> is deleted successfully,
     <code>false</code> if permission is denied or deletion is unsuccessful.
    */
    public boolean deleteGroup(Group group, String callerId)
    {
        return false;
    }

    /**
     *Add an existing user to a group.
     *@param user the User object to be added to a group
     *@param group the group 
     *@param isPrimary indicating whether this group is going to be the primary group for this user
     * a user may only have one primary group
     *@return <code>true</code> if the addition is successful,
     * <code>false</code> if permission is denied, or addition is unsuccessful
     */
    public boolean addUserToGroup(User user, Group group, 
                                  String callerId, boolean isPrimary)
    {
        return false;
    }

    /**
     *deleting the relationship between an existing user and a group.
     *@param user the User object to be removed to a group
     *@param group the group 
     *@return <code>true</code> if the deletion is successful,
     * <code>false</code> if permission is denied, or deletion is unsuccessful
     * Note: deletion will not be successful if the group is the user's primary group
     */
    public boolean deleteUserFromGroup(User user, Group group, 
                                       String callerId)
    {
        return false;
    }

    /**
     * set the primary group of a user. If currently another group
     *  is the primary group of the user, 
     * that group will be cleared of the primary role, since a 
     * user may only have one and only one primary
     * group.
     *@param user The User object for which a primary group is to be set
     *@param group The Group object which will be assigned as the
     *  primary group for the user
     *@param callerId the ID of the user who calls this method
     *@return <code>true</code> if the primary group is set successfully,
     * <code>false</code> if the permission is denied or the
     *   group setting is unsuccessful.
     */
    public boolean setPrimaryGroup (User user, Group group, String callerId)
    {
        return false;
    }
 
    /**
     * @return an object implementing the User interface given a user Id,
     * <code>null</code> if does not exist or error occurs.
     * @param id the user Id
     */
    public User getUser(String id) 
        throws SecurityServiceException
    {
        return new FakeUser();
    }
    
    /**
     * @return an object implementing the User interface given a user name,
     * for the "guest" user
     * <code>null</code> if does not exist or error occurs.
     */
    public User getGuestUser()
        throws SecurityServiceException
    {
        return getUser("guest");
    }
    
    /**
     * @return an object implementing the User interface given a user name,
     * for the administration user
     * <code>null</code> if does not exist or error occurs.
     */
    public User getAdminUser()
        throws SecurityServiceException
    {
        return getUser("admin");
    }


    /**
     * @param name the group name
     * @return an object implementing the Group interface given the group name,
     * <code>null</code> if does not exist or error occurs.
     */
    public Group getGroup(String name)
        throws SecurityServiceException
    {
        return new FakeGroup();
    }

}

