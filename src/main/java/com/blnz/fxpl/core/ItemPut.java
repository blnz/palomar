package com.blnz.fxpl.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.blnz.fxpl.fs.FS;
import com.blnz.fxpl.fs.FsRepository;
import com.blnz.fxpl.fs.RepositoryUtil;
import com.blnz.fxpl.fs.Transaction;

import com.blnz.fxpl.fs.RepositoryItem;
import com.blnz.fxpl.fs.FsException;

import com.blnz.fxpl.FXRequest;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.security.Security;
import com.blnz.fxpl.security.User;
import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.util.pipe.LazyBufferedReaderWriter;
import com.blnz.fxpl.xform.TransformService;
import com.blnz.fxpl.xform.XForm;
import com.blnz.fxpl.xform.impl.OutStreamContentHandler;

import com.blnz.fxpl.log.Log;
import com.blnz.fxpl.log.Logger;

import com.blnz.xsl.om.ExtensionContext;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * fetch an xml document from the repository, and evaluate it as FXPL
 */
public class ItemPut extends FXRequestServerSide {

    /**
     * returns the base URI that should be associated with the XML document
     * which would result from evaluating this XRAPRequest in the given context
     */
    public String getURI(FXContext context) {
        try {
            RepositoryItem item = getItem(extendContext(context));
            return item.getBaseURI();
        } catch (Exception e) {
            // FIXME: do something
            return "nullURI";
        }
    }

    public void eval(ContentHandler responseTarget, ExtensionContext context)
            throws Exception {

        boolean inheritedTransaction = false;

        Logger logger = Log.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug(getTagName() + ": entry");
        }

        FXContext ctx = extendContext((FXContext) context);
        if (ctx == null) {
            throw new Exception("Null context");
        }

        FsRepository repos = FS.getRepository();

        AttributesImpl atts = new AttributesImpl();

        Transaction xact = null;

        try {
            if (repos == null) {
                throw new Exception("null DocumentManagement");
            }

            User user = getUser(ctx);

            // Get target item (directory to copy item to)
            String name = (String) context.get("name");
            if (name == null || name.length() == 0) {
                throw new Exception("null item name");
            }

            String path = (String) context.get("path");
            if (path == null) {
                throw new Exception("Target item path not specified.");
            }

            String cds = (String) context.get("createFolders");
            boolean createDirs = ("yes".equals(cds) || "true".equals(cds));

            if (logger.isDebugEnabled()) {
                logger.debug(getTagName() + ": path={" + path + "}, name={"
                        + name + "}");
            }

            String[] pparts = RepositoryUtil.splitPath(RepositoryUtil
                    .normalizePath(path, name));

            xact = (Transaction) context.get("currentTransaction");

            if (xact == null) {
                xact = repos.startTransaction();
                inheritedTransaction = false;
                if (!"false".equals(context.get("transactional"))) {
                    context.put("currentTransaction", xact);
                }
            } else {
                inheritedTransaction = true;
            }

            RepositoryItem parentDir;
            if (createDirs) {
                parentDir = repos.getOrCreateFolder(xact, user, pparts[0]);
            } else {

                parentDir = repos
                        .getRepositoryItemByPath(xact, user, pparts[0]);

            }

            if (parentDir == null) {
                throw new Exception("Cannot create " + pparts[0]
                        + ". Cannot read parent directory: " + pparts[0]);
            }

            String sourceID = (String) context.get("sourceID");
            String insertAs = (String) context.get("insertAs");
            String insertMimeType = (String) context.get("insertMimeType");

            if (sourceID == null) {
                // CMC: Modifiy
                // throw new Exception("Source item id not specified.");

                FXRequest xreq = getSubRequest(1);

                // cast the input as XMLReader
                FXRequestReaderAdapter src = new FXRequestReaderAdapter(
                        ((FXRequestServerSide) xreq).extendContext(ctx), xreq);

                if (insertAs != null && insertAs.equals("binary")) {

                    // create a new item and bind stdout to its stream

                    String mimeType = null;
                    if (insertMimeType == null || "".equals(insertMimeType)) {
                        mimeType = "image/jpeg";
                    } else {
                        mimeType = insertMimeType;
                    }

                    RepositoryItem newItem = parentDir.createChildBinaryItem(
                            user, pparts[1], mimeType);

                    if (newItem == null) {
                        throw new Exception("Cannot create binary item: "
                                + pparts[0] + "/" + pparts[1]
                                + ".  Check user permissions.");
                    }
                    newItem = repos.getRepositoryItem(xact, user,
                            newItem.getItemID());

                    OutputStream pos = new BufferedOutputStream(
                            newItem.getOutputStream());

                    context.put("stdout", pos);

                    xreq.eval(responseTarget, context);

                    pos.close();

                } else if (insertAs != null && insertAs.equals("html")) {
                    insertHTML(user, xact, pparts[1], src, parentDir);
                } else {
                    insertXML(user, xact, pparts[1], src, parentDir, false);
                }
            } else {
                // means we're doing a copy of an existing item
                RepositoryItem item = repos.getRepositoryItem(user, sourceID);
                if (item == null) {
                    throw new Exception("Cannot create " + pparts[0]
                            + ". Check user permissions.");
                }

                insertItem(user, xact, ctx, item, parentDir, false);
            }

            if (!inheritedTransaction) {
                xact.commit();
            }

            atts.addAttribute("", "name", "name", "CDATA",
                    RepositoryUtil.normalizePath(pparts[0], pparts[1]));
            startElement("success", atts, responseTarget);
            endElement("success", responseTarget);
            if (logger.isDebugEnabled()) {
                logger.debug(getTagName() + ": exit, path={" + path
                        + "}, name={" + name + "}");
            }
        } catch (Exception ex) {

            ex.printStackTrace();
            if (xact != null && !inheritedTransaction) {
                try {
                    xact.rollback();
                } catch (Exception ex2) {
                }
            }
            errorResponse(ex, responseTarget, ctx);
        } finally {
            if (xact != null && !inheritedTransaction) {
                try {
                    xact.close();
                } catch (Exception ex2) {
                }
                xact = null;
            }
            context = null;
        }
    }

    // copies an existing item
    private void insertItem(User user, Transaction xact, FXContext ctx,
            RepositoryItem src, RepositoryItem target, boolean addDecl)
            throws Exception {
        // check read permisson on src
        if (!Security.getSecurityService().checkRead(user, src)) {
            throw new FsException("Cannot access "
                    + RepositoryUtil.normalizePath(src.getDirNamePath(),
                            src.getName()) + ". Permission denied");
        }

        // check write permisson on target directory
        if (!Security.getSecurityService().checkWrite(user, target)) {
            throw new FsException("Cannot write to "
                    + RepositoryUtil.normalizePath(target.getDirNamePath(),
                            target.getName()) + ". Permission denied");
        }

        // check if target item exists and check write permisson on the item
        RepositoryItem existingItem = src.getOwnerRepository()
                .getRepositoryItemByPath(
                        xact,
                        user,
                        RepositoryUtil.normalizePath(target.getDirNamePath(),
                                src.getName()));

        // check write permisson on existing item
        if (existingItem != null
                && !Security.getSecurityService()
                        .checkWrite(user, existingItem)) {
            throw new FsException("Cannot overwrite "
                    + RepositoryUtil.normalizePath(
                            RepositoryUtil.normalizePath(
                                    target.getDirNamePath(), target.getName()),
                            src.getName()) + ". Permission denied");
        }

        int itemType = src.getItemType();

        if (itemType == RepositoryItem.FOLDER) {
            createFolder(user, target, src.getName());
        } else if (itemType == RepositoryItem.TEXT) {
            insertText(user, xact, src, target);
        } else if (itemType == RepositoryItem.BINARY) {
            insertBinary(user, xact, src, target);
        } else if (itemType == RepositoryItem.XML) {
            FXRequest xreq = getSubRequest(1);

            // cast the input as XMLReader (for flatFile spec?)
            FXRequestReaderAdapter reader = new FXRequestReaderAdapter(
                    ((FXRequestServerSide) xreq).extendContext(ctx), xreq);
            String path = (String) ctx.get("path");
            if (path == null) {
                throw new Exception("Target item path not specified.");
            }
            insertXML(user, xact, src.getName(), reader, target, addDecl);
        } else {
            throw new Exception("Item type not supported.");
        }
    }

    /**
 *
 */
    private void insertBinary(User user, Transaction xact, RepositoryItem src,
            RepositoryItem target) throws Exception {
        RepositoryItem newItem = target.createChildBinaryItem(user,
                src.getName(), src.getMimeType());

        if (newItem == null) {
            throw new Exception("Cannot copy " + src.getName()
                    + ".  Check user permissions.");
        }
        newItem = src.getOwnerRepository().getRepositoryItem(xact, user,
                newItem.getItemID());

        BufferedInputStream bis = new BufferedInputStream(src.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(
                newItem.getOutputStream());
        transportItem(bis, bos);
    }

    /**
 *
 */
    private void insertXML(User user, Transaction xact, String srcName,
            XMLReader src, RepositoryItem parentItem, boolean addDecl)
            throws Exception {
        // parse the XML into a buffer:
        File tempFile = ConfigProps.getFiles().createTempFile();

        LazyBufferedReaderWriter lbrw = new LazyBufferedReaderWriter(tempFile);

        TransformService ts = XForm.getTransformService();
        ContentHandler handler = ts.createOutputStreamContentWriter(lbrw,
                addDecl);

        src.setContentHandler(handler);

        handler.startDocument();
        src.parse("");
        handler.endDocument();

        lbrw.flush();
        lbrw.close();

        // we got here, seems like it parsed okay

        RepositoryItem item = parentItem.createChildXMLItem(user, srcName,
                "application/xml");

        if (item == null) {
            throw new Exception("Cannot create " + srcName
                    + ".  Check user permissions.");
        }

        Reader in = lbrw.getReader();
        OutputStream out = item.getOutputStream();
        char[] buf = new char[1024 * 4];
        while (true) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }
            String s = new String(buf, 0, len);
            out.write(s.getBytes(LazyBufferedReaderWriter.ENCODING));
        }
        in.close();
        out.close();

        // having first stored a literal image of the content, we'll next
        // send the sax events representing that same document
        // once more, this time we index it
        in = lbrw.getReader();

        InputSource psrc = new InputSource(in);
        handler = item.openXMLWriter();
        if (handler instanceof OutStreamContentHandler) {
            ((OutStreamContentHandler) handler).setOmitDecl(!addDecl);
        }
        XMLReader rdr = ts.createInputSourceReader();
        rdr.setContentHandler(handler);
        rdr.parse(psrc);

        lbrw.clean();

    }

    /**
 *
 */
    private void insertHTML(User user, Transaction xact, String srcName,
            XMLReader src, RepositoryItem target) throws Exception {
        File tempFile = ConfigProps.getFiles().createTempFile();

        OutputStreamWriter sw = new OutputStreamWriter(new FileOutputStream(
                tempFile), "ISO-8859-1");
        TransformService ts = XForm.getTransformService();

        ContentHandler handler = ts
                .createHTMLContentWriter(new PrintWriter(sw));
        src.setContentHandler(handler);
        handler.startDocument();
        src.parse("");
        handler.endDocument();

        // delete item if it already exists in repository
        String path = RepositoryUtil.normalizePath(target.getDirNamePath(),
                target.getName());
        path += "/" + srcName;
        RepositoryItem newItem = target.getOwnerRepository()
                .getRepositoryItemByPath(xact, user, path);
        if (newItem != null) {
            newItem.deleteSelf();
        }

        newItem = target.createChildTextItem(user, srcName, "text/html");

        if (newItem == null) {
            throw new Exception("Cannot create " + srcName
                    + ".  Check user permissions.");
        }
        newItem = target.getOwnerRepository().getRepositoryItem(xact, user,
                newItem.getItemID());

        int bufferSize = 1024 * 4;
        BufferedReader in = new BufferedReader(new FileReader(tempFile),
                bufferSize);

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                newItem.getOutputStream(), "ISO-8859-1"), bufferSize);
        char[] buf = new char[bufferSize];
        while (true) {
            int len = in.read(buf);
            if (len == -1)
                break;
            out.write(buf, 0, len);
        }

        in.close();
        out.flush();
        out.close();
        sw.close();

        // delete temp file
        tempFile.delete();
    }

    /**
 *
 */
    private void insertText(User user, Transaction xact, RepositoryItem src,
            RepositoryItem target) throws Exception {
        RepositoryItem item = target.createChildTextItem(user, src.getName(),
                src.getMimeType());

        transportItem(src.getInputStream(), item.getOutputStream());
    }

    /**
 *
 */
    private void createFolder(User user, RepositoryItem parentDir, String name)
            throws Exception {
        RepositoryItem item = parentDir.createChildDirItem(user, name);
    }

    /**
 *
 */
    private void transportItem(InputStream src, OutputStream sink)
            throws IOException {
        try {
            int b = -1;
            while ((b = src.read()) != -1) {
                sink.write(b);
            }
        } finally {
            src.close();
            sink.flush();
            sink.close();
        }
    }

}
