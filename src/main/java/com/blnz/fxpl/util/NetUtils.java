package com.blnz.fxpl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class NetUtils {
    
    /**
     * Array containing the safe characters set as defined by RFC 1738
     * safeCharacters,  hexadecimal taken from Cocoon's 
     * org.apache.cocoon.util.NetUtils
     */
    private static BitSet safeCharacters;

    private static final char[] hexadecimal =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'A', 'B', 'C', 'D', 'E', 'F'};

    static {
        safeCharacters = new BitSet(256);
        int i;
        // 'lowalpha' rule
        for (i = 'a'; i <= 'z'; i++) {
            safeCharacters.set(i);
        }
        // 'hialpha' rule
        for (i = 'A'; i <= 'Z'; i++) {
            safeCharacters.set(i);
        }
        // 'digit' rule
        for (i = '0'; i <= '9'; i++) {
            safeCharacters.set(i);
        }

        // 'safe' rule
        safeCharacters.set('$');
        safeCharacters.set('-');
        safeCharacters.set('_');
        safeCharacters.set('.');
        safeCharacters.set('+');

        // 'extra' rule
        safeCharacters.set('!');
        safeCharacters.set('*');
        safeCharacters.set('\'');
        safeCharacters.set('(');
        safeCharacters.set(')');
        safeCharacters.set(',');

        // special characters common to http: file: and ftp: URLs ('fsegment' and 'hsegment' rules)
        safeCharacters.set('/');
        safeCharacters.set(':');
        safeCharacters.set('@');
        safeCharacters.set('&');
        safeCharacters.set('=');
    }

    public static String absolutize(String parent, String url) {
        int protocolPos = parent.indexOf("://");
        if (url.startsWith("mailto:") ||url.startsWith("javascript:")) {
            return url;
        } else if (url.indexOf("://") > 0) {
            return normalize(url);
        } else if (url.startsWith("//")) {
            return normalize(parent.substring(0, protocolPos+1) + url);
        } else if (url.startsWith("/")) {
            int domainEnd = parent.indexOf("/", protocolPos+3);
            String base = domainEnd < 0 ? parent : parent.substring(0, domainEnd);
            return normalize(base + url);
        } else if (parent.endsWith("/")) {
            return normalize(parent + url);
        } else if (protocolPos>-1 && parent.indexOf('/', protocolPos+3)<0) {
            return normalize(parent + '/' + url);
        } else if (url.startsWith("#")) {
            return parent;
        } else {
            return normalize(removeFilename(parent) + url);
        }
    }
    
    /**
     * Removes the anchor part of a URL
     * @param url
     */
    public static String deanchor(String url) {
        int hashPos = url.indexOf('#');
        if (hashPos == -1) {
            return url;
        } else {
            return url.substring(0,hashPos);
        }
    }

    /**
     * Encode a path as required by the URL specification (<a href="http://www.ietf.org/rfc/rfc1738.txt">
     * RFC 1738</a>). This differs from <code>java.net.URLEncoder.encode()</code> which encodes according
     * to the <code>x-www-form-urlencoded</code> MIME format.
     *
     * Taken from Cocoon's org.apache.cocoon.util.NetUtils
     * @param path the path to encode
     * @return the encoded path
     */
    public static String encodePath(String path) {
       // stolen from org.apache.catalina.servlets.DefaultServlet ;)

        /**
         * Note: This code portion is very similar to URLEncoder.encode.
         * Unfortunately, there is no way to specify to the URLEncoder which
         * characters should be encoded. Here, ' ' should be encoded as "%20"
         * and '/' shouldn't be encoded.
         */

        int maxBytesPerChar = 10;
        StringBuffer rewrittenPath = new StringBuffer(path.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(buf, "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
            writer = new OutputStreamWriter(buf);
        }

        for (int i = 0; i < path.length(); i++) {
            int c = path.charAt(i);
            if (safeCharacters.get(c)) {
                rewrittenPath.append((char)c);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write(c);
                    writer.flush();
                } catch(IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    byte toEncode = ba[j];
                    rewrittenPath.append('%');
                    int low = (toEncode & 0x0f);
                    int high = ((toEncode & 0xf0) >> 4);
                    rewrittenPath.append(hexadecimal[high]);
                    rewrittenPath.append(hexadecimal[low]);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }

    /**
     * Normalize a uri containing ../ and ./ paths.
     * (code taken from Apache Cocoon (org.apache.cocoon.util.NetUtils)
     *
     * @param uri The uri path to normalize
     * @return The normalized uri
     */
    public static String normalize(String uri) {
        if ("".equals(uri)) {
            return uri;
        }
        String protocol;
        String host;
        if (uri.indexOf("://")>0) {
            int protocolEnd = uri.indexOf("://")+3;
            int hostEnd = uri.indexOf('/', protocolEnd+1)+1;
            if (hostEnd ==0) {
                return uri + "/";
            }
            protocol = uri.substring(0, protocolEnd);
            host = uri.substring(protocolEnd, hostEnd).toLowerCase();
            uri = uri.substring(hostEnd);
            if ("".equals(uri)) {
                return protocol + host;
            }
        } else {
            protocol = "";
            host="";
        }
        int leadingSlashes = 0;
        for (leadingSlashes = 0 ; leadingSlashes < uri.length()
                && uri.charAt(leadingSlashes) == '/' ; ++leadingSlashes) {}
        boolean isDir = (uri.charAt(uri.length() - 1) == '/');
        StringTokenizer st = new StringTokenizer(uri, "/");
        LinkedList clean = new LinkedList();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("..".equals(token)) {
                if (! clean.isEmpty() && ! "..".equals(clean.getLast())) {
                    clean.removeLast();
                    if (! st.hasMoreTokens()) {
                        isDir = true;
                    }
                } else if (!"".equals(protocol) && clean.isEmpty()) {
                    // do nothing
                } else {
                    clean.add("..");
                }
            } else if (! ".".equals(token) && ! "".equals(token)) {
                clean.add(token);
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append(host);
        while (leadingSlashes-- > 0) {
            sb.append('/');
        }
        for (Iterator it = clean.iterator() ; it.hasNext() ; ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append('/');
            }
        }
        if (isDir && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        return sb.toString();
    }

    public static String removeFilename(String url) {
        int startPos = 0;
        if (url.startsWith("http://")) {
            startPos = "http://".length()+1;
        } else if (url.startsWith("file:///")) {
            startPos = "file:///".length()+1;
        } else if (url.startsWith("file:/")) {
            startPos = "file:/".length()+1;
        }
        if (url.indexOf('/', startPos) > -1) {
            return url.substring(0, url.lastIndexOf('/')+1);
        } else {
            return url;
        }
    }
    
    public static String getFilename(String url) {
        int startPos = 0;
        if (url.startsWith("http://")) {
            startPos = "http://".length()+1;
        }
        if (url.indexOf("/", startPos) > -1) {
            return url.substring(url.lastIndexOf("/")+1);
        } else {
            return "";
        }
    }

    /**
     * Remove path and file information from a filename returning only its
     * extension
     * (code taken from Apache Cocoon (org.apache.cocoon.util.NetUtils)
     *
     * @param uri The filename
     * @return The filename extension (with starting dot!) or null if filename extension is not found
     */
    public static String getExtension(String uri) {
        int dot = uri.lastIndexOf('.');
        if (dot > -1) {
            uri = uri.substring(dot);
            int slash = uri.lastIndexOf('/');
            if (slash > -1) {
                return null;
            } else {
                int sharp = uri.lastIndexOf('#');
                if (sharp > -1) {
                    // uri starts with dot already
                    return uri.substring(0, sharp);
                } else {
                    int mark = uri.lastIndexOf('?');
                    if (mark > -1) {
                        // uri starts with dot already
                        return uri.substring(0, mark);
                    } else {
                        return uri;
                    }
                }
            }
        } else {
            return null;
        }
    }
}
