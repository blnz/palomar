
package com.blnz.fxpl.fs.impl;

/**
 * maps a global identifier or name to local identifier or name, and vice-versa
 */
public interface IDStringLocalizer
{
    public String localize(String global);
    public String globalize(String local);
}