package com.blnz.fxpl.security;

/**
 * Interface to check and set Permissions.
 *
 * It represents a Unix-Style authorization scheme.
 * Client code can provide an implementation of this and ask the security
 *  service to validate a user's authority to perform a given operation on the
 *  object.
 */
public interface PermissionSet
{
    /**
     * Obtain a PermBits object representing access permissions.
     * The PermBits should contain 32 bits with this interpretation:<br>
     <ul>
     <li>bit 0:  owner Read</li>
     <li>bit 1:  owner Write</li>
     <li>bit 3:  owner Execute</li>
     <li>bit 4:  Reserved for future ussage</li>
     <li>bit 5:  group Read</li>
     <li>bit 6:  group Write</li>
     <li>bit 7:  group Execute</li>
     <li>bit 8:  Reserved for future usage</li>
     <li>bit 9:  World Read</li>
     <li>bit 10: World Write</li>
     <li>bit 11: World Execute</li>
     <li>bit12-31: Reserved for future use</li>
     </ul>
     *
     * @return a PermBits object representing the permission
     */ 
    public PermBits getPermissions();

    /**
     *@return the ID of the owner of the item with the above permission bits.
     */
    public String getOwnerID();

    /**
     *@return the ID of the owner group of the item with the above permission bits.
     */
    public String getOwningGroupID();
}

