package com.blnz.fxpl.security;

import java.util.BitSet;

public class PermBits extends BitSet
{

    /**
     * constructs a 32 bit BitSet to represent permissions.
     *
     * @param flags 32 bits represented as an integer
     */
    public PermBits(int flags)
    {
        super(32);
        setBits(this, flags);
    }

    /**
     * returns an integer representation of the 32 bits
     */
    public int toInt()
    {
        return toInt(this);
    }

    /**
     * converts an int into a 32 bit BitSet
     */
    public static BitSet toBits(int src)
    {
        int mask = 1;
        BitSet set = new BitSet(32);
        setBits(set, src);
        return set;
    }

    /**
     * converts a Bitset into an int
     */
    public static int toInt(BitSet src)
    {
        int mask = 1;
        int set = 0;
        for (int i = 0; i < 32; ++i) {
            if (src.get(i)) {
                set |= mask;
            }
            mask <<= 1;
        }
        return set;
    }

    // 
    static private void setBits(BitSet set, int src)
    {
        int mask = 1;

        for (int i = 0; i < 32; ++i) {
            if ((src & mask) != 0) {
                set.set(i);
            }
            mask <<= 1;
        }
    }
}
