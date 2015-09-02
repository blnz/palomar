package com.blnz.fxpl.util;

public class StringUtils {
    
    /** escapes the XML characters in CDATA nececessary for well-formedness
     */
    public static String encodeXmlString(String s)
    {
        
        byte[] buf = s.getBytes();
        int len = buf.length;
        int off = 0;
        int op = 0;

        byte[] obuf = new byte[len * 6];

        while (--len >= 0) {
            byte b = buf[off++];
            switch (b) {
            case (byte)'&':
                obuf[op++] = (byte) '&';
                obuf[op++] = (byte) 'a';
                obuf[op++] = (byte) 'm';
                obuf[op++] = (byte) 'p';
                obuf[op++] = (byte) ';';
                break;
            case (byte)'<':
                obuf[op++] = (byte) '&';
                obuf[op++] = (byte) 'l';
                obuf[op++] = (byte) 't';
                obuf[op++] = (byte) ';';
                break;
            case (byte)'>':
                obuf[op++] = (byte) '&';
                obuf[op++] = (byte) 'g';
                obuf[op++] = (byte) 't';
                obuf[op++] = (byte) ';';
                break;
            case (byte)'"':
                obuf[op++] = (byte) '&';
                obuf[op++] = (byte) 'q';
                obuf[op++] = (byte) 'u';
                obuf[op++] = (byte) 'o';
                obuf[op++] = (byte) 't';
                obuf[op++] = (byte) ';';
                break;
            default:
                obuf[op++] = b;
                break;
            }
        }
        return new String(obuf, 0, op);
    }

    /**
     * @return the current calendar year.
     *
     * note: this method is used by stylesheet querymanager/tpFaml.xsl.
     */
    public static int getCurrYear()
    {
    return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }


    /** convert an integer to a string and prefix it with a specified
     * character.
     * @param i an integer number to be converted to string
     * @param len length of the string
     * @param ch prefix the string with this char
     */
    public static String prefixInt(int i, int len, char ch)
    {
    StringBuffer s = new StringBuffer(String.valueOf(i));
    while (s.length() < len) {
        s.insert(0, ch);
    }
    return s.toString();
    }

}
