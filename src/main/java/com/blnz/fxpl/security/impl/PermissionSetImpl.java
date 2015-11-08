package com.blnz.fxpl.security.impl;

import com.blnz.fxpl.security.PermissionSet;
import com.blnz.fxpl.security.PermBits;

public class PermissionSetImpl implements PermissionSet 
{
    private String _ownerId = "";
    private String _groupId = "";
    private PermBits _permissions = null;

    public PermissionSetImpl(String ownerId, String groupId, PermBits permissions) 
    {
        _ownerId = ownerId;
        _groupId = groupId;
        _permissions = permissions;
    }

    /**
     *Use this method to obtain a BitSet object representing access permissions.
     *The BitSet should contain 32 bits, and here are their representations:<br>
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
     *@return a BitSet object representing the permission
     */ 
    public PermBits getPermissions() 
    {
        return _permissions;
    }

    /**
     *@return the ID of the owner of the item with the above permission bits.
     */
    public String getOwnerID() 
    {
        return _ownerId;
    }

    /**
     *@return the ID of the owner group of the item with the above permission bits.
     */
    public String getOwningGroupID() 
    {
        return _groupId;
    }

}
