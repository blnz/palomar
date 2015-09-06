package com.blnz.fxpl.shell;

import com.blnz.fxpl.FXHome;
import com.blnz.fxpl.FXConnection;
import com.blnz.fxpl.FXContext;
import com.blnz.fxpl.FXException;

import com.blnz.fxpl.util.ConfigProps;
import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;

import java.util.StringTokenizer;

import java.io.File;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;


/**
 *
 */
public class FXShell
{

    private char[] _buf = new char[1024];
    private FXConnection _xfy;

    /**
     *  constructor attempts to obtain a configured instance of
     *  an SixleService
     */
    public FXShell(String xfyServiceName, 
                   String userName, String password)
    {
        init(xfyServiceName, userName, password);
    }
    
    /**
     *  constructor attempts to obtain a configured instance of
     *  a local SixleService
     */
    public FXShell()
    {
        init("local", "admin", "admin");
    }

    private void init(String xfyServiceName, 
                      String userName, 
                      String password)
    {
        try {
            String xfs = "com.blnz.fxpl.fs.path";
            String reposLoc = System.getProperty("com.blnz.fxpl.fs.path"); 
            if (reposLoc != null) {
                System.out.println("setting fs.fs repository to: [" + reposLoc + "]"); 
                ConfigProps.setProperty(xfs, reposLoc);
            }
            _xfy = FXHome.getFXConnection(xfyServiceName, 
                                          userName, 
                                          password);
        } catch (FXException e) {
            System.err.println("failed to obtain FXPLService: " +
                               xfyServiceName);
            showEchoException(e);
            System.exit(1);
        }
    }

    /**
     * main loop
     */
    public void readEvalPrintLoop()
    {
        
        String line = null;
        boolean done = false;

        BufferedReader r = 
            new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Type '?' for help");

        while (!done) {
            // print command prompt

            System.out.print("\nX->");
            try {
                line = r.readLine();

            } catch (java.io.IOException ex) {
                done = true;
            }

            if (line == null) {
                done = true;
            } else if  (line.length() == 0) {
                continue;
            } else {
                // break the command line into tokens
                StringTokenizer st = new StringTokenizer(line, " ");

                // the first token is the command
                String command = st.nextToken();
               
                // switch on the command, call the appropriate
                //  handler with the rest of the tokens
                if ("list".equals(command)) {
                    // list(st);
		} else if ("eval".equals(command) || "e".equals(command)) {
                    eval(st);
                } else if ("help".equals(command) || "?".equals(command)) {
                    help();
                } else if ("quit".equals(command) || "q".equals(command)) {
                    done = true;
                } else {
                    System.out.println("\nunrecognized command: " +
                                       command);
                }
            }
        }
    }


    //
    public void eval(StringTokenizer st)
    {

        try { 
            String tok = null;
            String outFile = null;
	    String readFile = null;
	    
            // parse out the command options
            boolean opts = true;
            while (opts && st.hasMoreTokens()) {
                tok = st.nextToken();
                if ("-out".equals(tok)) {
                    outFile = st.nextToken();
		} else if ("-read".equals(tok)) {
		    readFile = st.nextToken();
		} else {
                    opts = false;
                }
            }
	    
	    String request = null;
	    
	    if (readFile != null){
                // FIXME: implement
	    } else {
		// last argument is the echo request
		request = tok;
		while (st.hasMoreTokens()) {
		    request += " " + st.nextToken();
		}

                StringReader sr = new StringReader("<?xpsml?>" + request + ";");
                XPSMLReader xr = new XPSMLReader(sr);
                StringWriter sw = new StringWriter();
                int len;
                while ((len = xr.read(_buf)) > 0) {
                    sw.write(_buf, 0, len);
                }
                request = sw.toString();
	    }
	    
            Writer out;
            if (outFile != null) {
                out = new FileWriter(new File(outFile));
            } else {
                // we don't want to close System.out, so wrap it
                out = new NoClosePrintWriter(new PrintWriter(new OutputStreamWriter(System.out)));
            }
            FXContext context = _xfy.getExtendedContext();
            // somplace to put the output
            ContentHandler ch = XForm.createCharacterContentWriter(out);
            System.out.println("FXShell::eval() will eval [" + request + "]");

            _xfy.evalFXString(request, ch, context);
	    
	} catch (FXException ex) {
            showEchoException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }

    
    private void help()
    {
        System.out.println("recognized commands:");

        System.out.println("\n  quit (or 'q') ");
        System.out.println("     exit");

        System.out.println("\n  eval (or 'e') ");
	System.out.println("    evaluate an FX command");

    }

    
    private void showEchoException(FXException ex)
    {
        System.out.println("\nXFY-Error: " + ex.getMessage());
        ex.printStackTrace();
        
        Exception cause = ex.getException();
        if (cause != null) {
            // this exception wraps another
            System.out.println("underlying cause: " + 
                               cause.getMessage());

            if (cause instanceof FXException) {
                // wraps another FXException, recurse
                showEchoException((FXException) cause);
            } else {
                cause.printStackTrace();
            }
        }
    }

    /**
     * constructs an instance and enters a read - eval - print loop
     */
    public static void main(String[] args)
    {
        String server = "local";
        String userName = "admin";
        String password = "admin";

        String command = "";
        int i = 0;

        while ( i < (args.length) ) {
            if ("-s".equals(args[i]) ||
                "-server".equals(args[i])) {
                ++i;
                server = args[i++];
            } else if ("-u".equals(args[i]) ||
                       "-user".equals(args[i])) {
                ++i;
                userName = args[i++];
            } else if ("-p".equals(args[i]) ||
                       "-password".equals(args[i])) {
                ++i;
                password = args[i++];
            } else {
                break;
            }
        }

        while ( i < (args.length) ) {

            if (command.length() > 0) {
                command = command + " ";
            }
            command = args[i++];
        }


        FXShell ads = new FXShell(server, userName, password);

        if (command.length() == 0) {
            ads.readEvalPrintLoop();
        } else {
            ads.eval(new StringTokenizer(command));
        }
        
        System.exit(0);
    }
    
}

