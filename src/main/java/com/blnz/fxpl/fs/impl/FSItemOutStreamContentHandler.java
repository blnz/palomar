//$Id: XStoreItemOutStreamHandler.java,v 1.11 2005/02/20 00:42:47 blindsey Exp $

package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.Transaction;

import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.xform.impl.OutStreamContentHandler;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.io.OutputStream;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Hashtable;

/**
 * Use this class to open a content handler to store an XML document in the
 * repository. Currently being used by the FSRepositoryItem class.
 */
public class FSItemOutStreamContentHandler extends OutStreamContentHandler {

    FSRepositoryItem _fsItem = null;

    Transaction _xact = null;

    public FSItemOutStreamContentHandler(FSRepositoryItem fsItem, OutputStream os) {
        super(os);
        _fsItem = fsItem;
    }

    /**
     * handle SAX endDocument() event
     */
    public void endDocument() throws SAXException {
        super.endDocument();

        File f;
        try {
            f = FSConnection.getResource(_fsItem.getDirNamePath()
                    + File.separator + _fsItem.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new SAXException(e);
        }

        if (f == null || !f.exists()) {
            throw new SAXException("Unable to store the file in the file system");
        }
    }
}
