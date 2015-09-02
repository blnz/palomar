// $Id: Base64Encode.java 46 2004-12-11 00:39:16Z blindsey $

package com.blnz.fxpl.util;

/**
 * Base64 Encoding.
 * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
 */
public class Base64Encode
{
    public static final char PadChar = '=';

    private static final char[] EncodeMapChar = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    private static final byte[] EncodeMap = new byte[128];
    private static final byte[] DecodeMap = new byte[128];

    static {
        for (int i = 0; i < EncodeMapChar.length; i++) {
	    EncodeMap[i]  = (byte)EncodeMapChar[i];
            DecodeMap[EncodeMap[i]] = (byte)i;
        }
    }

//     /**
//      * Decodes the given string using the base64-encoding specified in
//      * RFC-1521 (Section 5.2).
//      *
//      * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
//      * @param string the string to decode.
//      * @return the decoded string or null if there was a problem decoding
//      */
//     public static String decode(String string)
//     {
//         byte[] in  = string.getBytes();
//         byte[] out = decode(in, 0, in.length);
//         if (out == null) {
//             return null;
//         }
//         return new String(out);
//     }

    /**
     * Decodes the given string using the base64-encoding specified in
     * RFC-1521 (Section 5.2).
     *
     * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
     * @param string the string to decode.
     * @return the decoded string or null if there was a problem decoding
     */
    public static byte[] decode(String string)
    {
        byte[] in  = string.getBytes();
        return  decode(in, 0, in.length);
    }

    /**
     * Decodes the given byte[] using the base64-encoding specified in
     * RFC-1521 (Section 5.2).
     *
     * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
     * @param  data the base64-encoded data.
     * @param  start the position in the array to start.
     * @param  len the number of bytes to decode.
     * @return the decoded data.
     */
    public static final byte[] decode(byte[] data, int start, int len)
    {
        if (data == null) {
            return  null;
        }

        if ((len % 4) != 0) {
            return null;
        }

        int tailPos = len;
        while (data [start + tailPos - 1] == PadChar) {
            tailPos--;
        }

        byte dest[] = new byte[tailPos - len/4];

	// convert from 64 letter alphabet to 0-63 number
        for (int i = start; i < len; i++) {
            data [i] = DecodeMap[data [i]];
        }

	// 4 byte to 3 byte conversion
        int sourceIndex = start;
        int destIndex   = 0;
        for (; destIndex < dest.length-2; sourceIndex += 4, destIndex += 3) {
	    // 6 bytes from first char + 2 of next char
            dest[destIndex]   = (byte)(((data [sourceIndex] << 2) & 255) |
                                       ((data [sourceIndex+1] >>> 4) & 003));

	    // last 4 bytes from second char + 4 of next char
            dest[destIndex+1] = (byte)(((data [sourceIndex+1] << 4) & 255) |
                                       ((data [sourceIndex+2] >>> 2) & 017));

	    // last 2 bytes from second char + 6 of next char
            dest[destIndex+2] = (byte)(((data [sourceIndex+2] << 6) & 255) |
                                       (data [sourceIndex+3] & 077));
        }

        if (destIndex < dest.length) {
            dest[destIndex]   = (byte)(((data[sourceIndex] << 2) & 255) |
                                       ((data[sourceIndex+1] >>> 4) & 003));
        }
        if (++destIndex < dest.length) {
            dest[destIndex]   = (byte)(((data[sourceIndex+1] << 4) & 255) |
                                       ((data[sourceIndex+2] >>> 2) & 017));
        }

        return dest;
    }

    /**
     * Encodes the given String using the base64-encoding specified in
     * RFC-1521 (Section 5.2). Makes no provisions for encoding more than
     * a line's worth of data (i.e. '\n' chars are not inserted into the
     * encode output so you should keep the (len-start) <= 57 bytes.
     * (This results in a maximum of (57/3)*4 or 76 characters per output
     * line.)
     *
     * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
     * @param  string data to be base64-encoded.
     * @return the encoded string or null if there was a problem encoding.
     */
    public static String encode(String string)
    {
        byte[] in  = string.getBytes();
        //         Log.getLogger().info("string.length=" + string.length());
        //         Log.getLogger().info("# of bytes=" + in.length);
	for (int i = 0; i < in.length; i++) {
            //        Log.getLogger().info("buf[" + i + "]=" + (int)in[i]);
	}
        byte[] out = encode(in, 0, in.length);
        if (out == null) {
            return null;
        }
        return new String(out);
    }

    /**
     * Encodes the given byte[] using the base64-encoding specified in
     * RFC-1521 (Section 5.2). Makes no provisions for encoding more than
     * a line's worth of data (i.e. '\n' chars are not inserted into the
     * encode output so you should keep the (len-start) <= 57 bytes.
     * (This results in a maximum of (57/3)*4 or 76 characters per output
     * line.)
     *
     * @see http://info.internet.isi.edu/in-notes/rfc/files/rfc1521.txt
     * @param  data data to be base64-encoded.
     * @param  start the position in the array to start.
     * @param  len the number of bytes to encode.
     * @return the encoded data.
     */
    public static final byte[] encode(byte[] data, int start, int len)
    {
        if (data == null) {
            return null;
        }

        int sourceIndex=start;
        int destIndex=0;

        byte dest[] = new byte[((len+2)/3)*4];

	// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
        for (; sourceIndex < len-2; sourceIndex += 3) {
            dest[destIndex++] = EncodeMap[(data[sourceIndex] >>> 2) & 077];
            dest[destIndex++] = EncodeMap[(data[sourceIndex+1] >>> 4) & 017 |
                                         (data[sourceIndex] << 4) & 077];
            dest[destIndex++] = EncodeMap[(data[sourceIndex+2] >>> 6) & 003 |
                                         (data[sourceIndex+1] << 2) & 077];
            dest[destIndex++] = EncodeMap[data[sourceIndex+2] & 077];
        }
        if (sourceIndex < start+len) {
            dest[destIndex++] = EncodeMap[(data[sourceIndex] >>> 2) & 077];
            if (sourceIndex < start+len-1) {
                dest[destIndex++] = EncodeMap[(data[sourceIndex+1] >>> 4) & 017 |
                                             (data[sourceIndex] << 4) & 077];
                dest[destIndex++] = EncodeMap[(data[sourceIndex+1] << 2) & 077];
            }
            else {
                dest[destIndex++] = EncodeMap[(data[sourceIndex] << 4) & 077];
            }
        }

	// add padding
        for (; destIndex < dest.length; destIndex++)
            dest[destIndex] = (byte)PadChar;

        return dest;
    }

}
