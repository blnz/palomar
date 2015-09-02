package com.blnz.fxpl.fs.impl;

import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.fs.RepositoryItem;

import com.blnz.fxpl.util.XPSMLReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;




/**
 * XMLReader for items in the repository
 * parse() invocation on this will generate SAX events
 * which can be handled by the the Contenthandler for this
 * reader.
 */
public class FSItemXMLReader extends ParserAdapter
{
    private RepositoryItem _xsItem = null;
    private InputSource _inputSource = null;
    private XMLReader _backup = null;

    /**
     * XMLReader to retrieve XML documents from the file system 
     * or the XML repository
     */
    public FSItemXMLReader(RepositoryItem xsItem) 
        throws SAXException
    {
        super(new com.blnz.xml.sax.Driver());
        _xsItem = xsItem;
        setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        setFeature("http://xml.org/sax/features/namespaces", true);
    }

    /** 
     * Creates its own input source from the XStoreItem 
     *  @param src pass a dummy input source. 
     */
    public void parse(InputSource src) 
        throws SAXException, IOException
    {
        if (_inputSource == null) {
            // first time we've been called
            retrieveXML();
            if (_inputSource == null){
                throw new SAXException("Cannot create a XStoreItemXMLReader : InputSource is null");
            }
            super.parse(_inputSource);
        } else {
          
            // somebody is trying to re-use us
            // FIXME: replace with the right kind of parser
            //   System.out.println("Wanna parse: " + s.getSystemId());
            if (_backup == null) {
                try {
                    _backup = XForm.createInputSourceReader();
                } catch (Exception ex) {
                    throw new SAXException(ex);
                }
                _backup.setContentHandler(getContentHandler());
            }
            _backup.parse(src);
        }
    }

    /** 
     * Creates its own input source from the XStoreItem 
     */
    public void parse(String sysID) 
        throws SAXException,  IOException
    {
        if (_inputSource == null) {
            retrieveXML();
            if (_inputSource == null){
                throw new SAXException("Cannot create a XStoreReader : InputSource is null");
            }
            //invoke parse
            super.parse(_inputSource);
        } else {
            // somebody is trying to re-use us
            // FIXME: replace with the right kind of parser
            //   System.out.println("Wanna parse: " + s.getSystemId());
            if (_backup == null) {
                try {
                    _backup = XForm.createInputSourceReader();
                } catch (Exception ex) {
                    throw new SAXException(ex);
                }
                _backup.setContentHandler(getContentHandler());
            }
            _backup.parse(sysID);
        }
    }
  
    /**
     * fetch a document from the filesystem or 
     * fetch the document from the XStore
     * depending on the status
     */
    private void retrieveXML()
        throws SAXException
    {

        try {
            retrieveFromFS();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }
    
    /** 
     * configures the input source to get the XML file from the file system
     */
    private void retrieveFromFS()
        throws Exception
    {
        if (_xsItem.getName().endsWith(".xpml")) {
            InputStreamReader sr = new InputStreamReader(_xsItem.getInputStream());
            XPSMLReader xr = new XPSMLReader(sr);
	    _inputSource = new InputSource(xr);
        } else  {
            _inputSource = new InputSource(_xsItem.getInputStream());
        }
        _inputSource.setSystemId(_xsItem.getBaseURI());
    }
    
}
