
package com.blnz.fxpl.fs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.ContentHandler;

import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;

import com.blnz.fxpl.xform.XForm;

/**
 * Repository utility class.
 */
public class RepositoryUtil 
{
    /**
     * Normalize path for a repository object.
     */
    public static String normalizePath(String path, String name) 
    {
        if (name != null) {
            if (name.startsWith("/")) {
                // ignore path if name has absolute path, i.e.,
                // starts with "/".
                path = null;
            }
	    
            if (name.endsWith("/")) {
                name = name.substring(0, name.length()-1);
            }
	}

        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }

        String result = null;
        if (path == null || path.equals("")) {
            result = name;
        } else if (name == null || name.equals("")) {
            result = path;
        } else {
            result = path + "/" + name;
        }
	
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
	
    
        return normalizePathString(result);
    }
    


    

    /**
     * returns a normalized string representing a file / folder
     * path, eliminating "." and ".." components as appropriate
     */
    public static String normalizePathString(String cat)
    {
	if (cat == null || cat.length() == 0) {
            return "";
        }
	
        StringTokenizer st = new StringTokenizer(cat, "/");
        int stl = st.countTokens();
        
        String[] strs = new String[stl];

        int o = 0;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok == null || "".equals(tok) || ".".equals(tok)) {
                // skip it
            } else if ("..".equals(tok)) {
                if (o > 0) {
                    --o;
                }
            } else {
                strs[o++] = tok;
            }
        }
        String result = "";
        if (cat.startsWith("/")) {
           result = "/";
        } 
        for (int i = 0; i < o; ++i) {
            result += strs[i];
            if (i < o - 1) {
                result += "/";
            }
        }
        return result;
    }
    
    /**
     * @return parent URI of the given URI.
     */
    public static String getParentUri(String uri) 
    {
        int index = uri.lastIndexOf("/");
        if (index == -1) {
            // shouldn't happen
            return null;
        } else if (index == 0) {
            return "/";
        } else {
            return uri.substring(0, index);
        }
    }

    /**
     * @return object name from a given object URI.
     */
    public static String getObjectName(String uri) 
    {
        // remove trailing "/"
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length()-1);
        }

        // use the last part of the uri as the object name
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    /**
     * Read bytes from input stream.
     */
    public static byte[] readFromStream(InputStream inputStream)
        throws IOException 
    {
        byte[] chunk = new byte[4096];
        byte[] all;
        int chunkLen;
        int allLen = 0;
        ArrayList chunks = new ArrayList();
        int i;
        int max;
        int ofs;
        
        chunkLen = inputStream.read(chunk);
        while (chunkLen != -1) {
            chunks.add(new Integer(chunkLen));
            chunks.add(chunk);
            allLen += chunkLen;
            chunk = new byte[4096];
            chunkLen = inputStream.read(chunk);
        }

        all = new byte[allLen];
        ofs = 0;
        max = chunks.size();
        for (i = 0; i < max; i += 2) {
            chunkLen = ((Integer) chunks.get(i)).intValue();
            chunk = (byte[]) chunks.get(i + 1);
            System.arraycopy(chunk, 0, all, ofs, chunkLen);
            ofs += chunkLen;
        }
        return all;
    }

    /**
     * splits a normalized path string into two components,
     * strs[0] == the path component and strs[1] == the name
     */
    public static String[] splitPath(String normalizedPath)
    {
        String[] parts = new String[2];
        parts[0] = "";

        if (normalizedPath == null || "".equals(normalizedPath)) {
            return parts;
        }
        int start = 0;
        int ix = normalizedPath.indexOf('/');
        while (ix != -1) {
            parts[0] += normalizedPath.substring(start, ix == 0 ? 1 : ix);
            start = ix + 1;
            ix = normalizedPath.indexOf('/', start);
            if (ix > 0 && start > 1) {
                parts[0] += "/";
            }
        }
        parts[1] = normalizedPath.substring(start);

        return parts;
    }
}
