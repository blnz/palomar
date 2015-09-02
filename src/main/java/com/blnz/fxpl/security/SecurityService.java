package com.blnz.fxpl.security;

/**
 * defines
 * permission checking and setting for a base set of operations on objects.
 * <p>the model resembles Unix file system permissions of read, write and
 *      execute,
 *  but those semantics are up to the application to decide</p>
 * <p>Also, like Unix, the concepts of users, groups, and a
 *  "superuser" are used.</p> 
 */

public interface SecurityService
{
    /**
     * Read permission
     */
    public static final String PERMISSION_READ = "Read";
    /**
     * Write permission
     */
    public static final String PERMISSION_WRITE = "Write";

    /**
     * Execute permission
     */
    public static final String PERMISSION_EXECUTE = "Execute";

    // built-in administrator
    public static final String ADMINISTRATOR = "admin";


    /**
     * authenticate the username and password.
     *@param userName username
     *@param password password
     *@return the User object representing the authenticated user
     * or <code>null</code> if unable to authenticate.
     */
    public User login(String userName, String password)
        throws SecurityServiceException;
    
    /**
     * authenticate using a hash code.
     *@param hash the hash code
     *@param password password
     *@return the User object representing the authenticated user
     * or <code>null</code> if unable to authenticate.
     */
    public User login(String hash)
        throws SecurityServiceException;
    
    /**
     *Check if a given user is an Administrator
     *@param userId the ID of the User
     *@return <code>true</code> if the user is an Administrator,
     * <code>false</code> if the user is not an Administrator
     */
    public boolean isAdministrativeUser(String userId);

    /**
     *check if a user has a certain permission.
     *@param user a User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted 
     *  permission information to be checked against.
     *@param perm a String indicating the permission to be checked.
     *  The valid value (case insensitive) are "read", "write", "execute".
     *@return <code>true</code> if the <code>perm</code> is granted
     *           to the <code>user</code>,
     *<code>false</code> if the <code>perm</code> is not granted to
     *           the <code>user</code>.
     */
    public boolean checkPermission(User user, PermissionSet mask, 
                                   String perm);

    /**
     *check if a user has a certain permission.
     *@param userId the ID of the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *              permission information to be checked against.
     *@param perm a String indicating the permission to be checked.
     *            The valid value (case insensitive) are "read",
     *             "write", "execute".
     *@return <code>true</code> if the <code>perm</code> is granted to
     *          the <code>userId</code>,
     *<code>false</code> if the <code>perm</code> is not granted to 
     *         the <code>userId</code>.
     */
    public boolean checkPermission(String userId, PermissionSet mask, 
                                   String perm);

    /**
     *check if a user has a "read" permission.
     *@param user the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the 
     *  granted permission information to be checked against.
     *@return <code>true</code> if the "read" permission is granted to 
     *               the <code>userId</code>,
     *<code>false</code> if the "read" permission is not granted
     *             to the <code>userId</code>.
     */
    public boolean checkRead(User user, PermissionSet mask);

    /**
     *check if a user has a "write" permission.
     *@param user the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *               permission information to be checked against.
     *@return <code>true</code> if the "write" permission is granted
     *                       to the <code>userId</code>,
     *<code>false</code> if the "write" permission is not granted
     *                  to the <code>userId</code>.
     */     
    public boolean checkWrite(User user, PermissionSet mask);
    
    /**
     *check if a user has an "execute" permission.
     *@param user the user whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *                 permission information to be checked against.
     *@return <code>true</code> if the "execute" permission is
     *                  granted to the <code>userId</code>,
     *<code>false</code> if the read "permission" is not granted
     to the <code>userId</code>.
    */
    public boolean checkExecute(User user, PermissionSet mask);
    
    /**
     *check if a user has a "read" permission.
     *@param userId the id of the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *                  permission information to be checked against.
     *@return <code>true</code> if the "read" permission is 
     *                  granted to the <code>user</code>,
     *<code>false</code> if the "read" permission is not granted to 
     *                  the <code>user</code>.
     */     
    public boolean checkRead(String userId, PermissionSet mask);

    /**
     *check if a user has a "write" permission.
     *@param userId the of the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the
     *             granted permission information to be checked against.
     *@return <code>true</code> if the "write" permission is granted
     *                       to the <code>user</code>,
     *<code>false</code> if the "write" permission is not
     *              granted to the <code>user</code>.
     */          
    public boolean checkWrite(String userId, PermissionSet mask);

    /**
     *check if a user has a "execute" permission.
     *@param userId the id of the User object whose permission needs to be checked.
     *@param mask a PermissionSet object containing all the granted
     *          permission information to be checked against.
     *@return <code>true</code> if the "execute" permission is
     *                  granted to the <code>user</code>,
     *<code>false</code> if the "execute" permission is not granted
     *                       to the <code>user</code>.
     */      
    public boolean checkExecute(String userId, PermissionSet mask);

    /**
     *set the default granted permission.
     *The default is Owner "read", "write", "execute"; Group "read" 
     *                    and World "read".
     *@param mask the PermissionSet object to be set to default values.
     */
    public void setDefault(PermissionSet mask);

    /**
     * set a Owner "Read" permission
     *@param mask the PermissionSet object to be set with
     *                   Owner "Read" permission
     */
    public void setOwnerRead(PermissionSet mask, boolean bit);

    /**
     * set a Owner "Write" permission
     *@param mask the PermissionSet object to be set with Owner
     * "Write" permission
     */
    public void setOwnerWrite(PermissionSet mask, boolean bit);

    /**
     * set a Owner "Execute" permission
     *@param mask the PermissionSet object to be set with 
     *                       Owner "Execute" permission
     */
    public void setOwnerExecute(PermissionSet mask, boolean bit);

    /**
     * set a Group "Read" permission
     *@param mask the PermissionSet object to be set with
     *                              Group "Read" permission
     */
    public void setGroupRead(PermissionSet mask, boolean bit);

   /**
     * set a Group "Write" permission
     *@param mask the PermissionSet object to be set with
     *                                Group "Write" permission
     */
    public void setGroupWrite(PermissionSet mask, boolean bit);

    /**
     * set a Group "Execute" permission
     *@param mask the PermissionSet object to be set with
     *                              Group "Execute" permission
     */
    public void setGroupExecute(PermissionSet mask, boolean bit);

   /**
     * set a World "Read" permission
     *@param mask the PermissionSet object to be set with
     *         World "Read" permission
     */
    public void setWorldRead(PermissionSet mask, boolean bit);

   /**
     * set a World "Write" permission
     *@param mask the PermissionSet object to be set with
     *                      World "Write" permission
     */
    public void setWorldWrite(PermissionSet mask, boolean bit);

    /**
     *set a World "Execute" permission
     *@param mask the PermissionSet object to be set with 
     *             World "Execute" permission
     */
    public void setWorldExecute(PermissionSet mask, boolean bit);

    /**
     *Add a new user to the database. Only the administrator is 
     *                allowed to do so.
     *@param userName the new User name to be added to the database.
     *@param password the new password of the user to be added.
     *@param callerId the userId for the user who calls this method.
     *@return <code>User</code> object if the <code>user</code> is
     *  added successfully,
     <code>null</code> if permission is denied or addition is unsuccessful.
    */
    public User addUser(String userName, String password, String callerId)
        throws SecurityServiceException;

    /**
     *Update a user's information. Only the administrator
     * or the very user himself are allowed to do so.
     *@param user the User object containing the new user
     *  information to be updated in the database.
     *@param callerId the userId for the user who calls this method.
     *@return <code>true</code> if the <code>user</code> is
     *  updated successfully,
     <code>false</code> if permission is denied or update is unsuccessful.
    */
    public boolean updateUser(User user, String callerId)
        throws SecurityServiceException;

    /**
     *Delete a user's entry from the database. Only the
     *  administrator is allowed to do so.
     *@param user the new User object to be deleted from the database.
     *@param callerId the userId for the user who calls this method.
     *@return <code>true</code> if the <code>user</code> is 
     *  deleted successfully,
     <code>false</code> if permission is denied or deletion is unsuccessful.
    */
    public boolean deleteUser(User user, String callerId)
        throws SecurityServiceException;

    /**
     *Add a new Group to the database. Only the administrator 
     * is allowed to do so.
     *@param groupName The new Group name to be added.
     *@param callerId the userId of the user who calls this method.
     *@return <code>Group</code> object if the
     *  <code>group</code> is added successfully,
     <code>null</code> if permission is denied or addition is unsuccessful.
    */
    public Group addGroup(String groupName, String callerId)
        throws SecurityServiceException;

    /**
     *Update a Group's information in the database. Only the
     *  administrator is allowed to do so.
     *@param group The Group object containing the updated information.
     *@param callerId the userId of the user who calls this method.
     *@return <code>true</code> if the <code>group</code> 
     * is updated successfully,
     <code>false</code> if permission is denied or update is unsuccessful.
    */
    public boolean updateGroup(Group group, String callerId)
        throws SecurityServiceException;

    /**
     *Delete a group from the database. Only the administrator
     *  is allowed to do so.
     *@param group The Group object to be deleted from the database.
     *@param callerId the userId of the user who calls this method.
     *@return <code>true</code> if the <code>group</code> is
     *  deleted successfully,
     <code>false</code> if permission is denied or deletion is unsuccessful.
    */
    public boolean deleteGroup(Group group, String callerId)
        throws SecurityServiceException;

    /**
     *Add an existing user to a group.
     *@param user the User object to be added to a group
     *@param group the group 
     *@param isPrimary indicating whether this group is going
     *  to be the primary group for this user
     * a user may only have one primary group
     *@return <code>true</code> if the addition is successful,
     * <code>false</code> if permission is denied, or addition is unsuccessful
     */
    public boolean addUserToGroup(User user, Group group, 
                                  String callerId, boolean isPrimary)
        throws SecurityServiceException;

    /**
     *deleting the relationship between an existing user and a group.
     *@param user the User object to be removed to a group
     *@param group the group 
     *@return <code>true</code> if the deletion is successful,
     * <code>false</code> if permission is denied, or deletion is unsuccessful
     */
    public boolean deleteUserFromGroup(User user, Group group, String callerId)
        throws SecurityServiceException;

    /**
     * set the primary group of a user. If currently another
     *  group is the primary group of the user, 
     * that group will be cleared of the primary role, since a user
     *  may only have one and only one primary
     * group.
     *@param user The User object for which a primary group is to be set
     *@param group The Group object which will be assigned as
     *  the primary group for the user
     *@param callerId the ID of the user who calls this method
     *@return <code>true</code> if the primary group is set successfully,
     * <code>false</code> if the permission is denied or
     *  the group setting is unsuccessful.
     */
    public boolean setPrimaryGroup(User user, Group group, String callerId)
        throws SecurityServiceException;

    /**
     * @return an object implementing the User interface given a user Id,
     * <code>null</code> if does not exist or error occurs.
     * @param id the user Id
     */
    public User getUser(String id)
        throws SecurityServiceException;
    
    /**
     * @return an object implementing the User interface given a user name,
     * for the "guest" user
     * <code>null</code> if does not exist or error occurs.
     */
    public User getGuestUser()
        throws SecurityServiceException;
    
    /**
     * @return an object implementing the User interface given a user name,
     * for the administration user
     * <code>null</code> if does not exist or error occurs.
     */
    public User getAdminUser()
        throws SecurityServiceException;
    
    /**
     * @param name the group's name
     * @return an object implementing the Group interface given the group name,
     * <code>null</code> if does not exist or error occurs.
     */
    public Group getGroup(String name)
        throws SecurityServiceException;
    
}
