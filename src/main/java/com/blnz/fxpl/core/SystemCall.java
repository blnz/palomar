package com.blnz.fxpl.core;

import com.blnz.xsl.om.ExtensionContext;
import com.blnz.fxpl.FXContext;

import com.blnz.fxpl.xform.XForm;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.io.StringReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * converts the string value of its content
 * to the infoset which results from parsing it as XML
 */
public class SystemCall extends FXRequestServerSide
{

    private static final String charEntities[][] = {
        { "00A0", "nbsp;"},
        { "00A1", "iexcl;"},
        { "00A2", "cent;"},
        { "00A3", "pound;"},
        { "00A4", "curren;"},
        { "00A5", "yen;"},
        { "00A6", "brvbar;"},
        { "00A7", "sect;"},
        { "00A8", "uml;"},
        { "00A9", "copy;"},
        { "00AA", "ordf;"},
        { "00AB", "laquo;"},
        { "00AC", "not;"},
        { "00AD", "shy;"},
        { "00AE", "reg;"},
        { "00AF", "macr;"},
        { "00B0", "deg;"},
        { "00B1", "plusmn;"},
        { "00B2", "sup2;"},
        { "00B3", "sup3;"},
        { "00B4", "acute;"},
        { "00B5", "micro;"},
        { "00B6", "para;"},
        { "00B7", "middot;"},
        { "00B8", "cedil;"},
        { "00B9", "sup1;"},
        { "00BA", "ordm;"},
        { "00BB", "raquo;"},
        { "00BC", "frac14;"},
        { "00BD", "frac12;"},
        { "00BE", "frac34;"},
        { "00BF", "iquest;"},
        { "00C0", "Agrave;"},
        { "00C1", "Aacute;"},
        { "00C2", "Acirc;"},
        { "00C3", "Atilde;"},
        { "00C4", "Auml;"},
        { "00C5", "Aring;"},
        { "00C6", "AElig;"},
        { "00C7", "Ccedil;"},
        { "00C8", "Egrave;"},
        { "00C9", "Eacute;"},
        { "00CA", "Ecirc;"},
        { "00CB", "Euml;"},
        { "00CC", "Igrave;"},
        { "00CD", "Iacute;"},
        { "00CE", "Icirc;"},
        { "00CF", "Iuml;"},
        { "00D0", "ETH;"},
        { "00D1", "Ntilde;"},
        { "00D2", "Ograve;"},
        { "00D3", "Oacute;"},
        { "00D4", "Ocirc;"},
        { "00D5", "Otilde;"},
        { "00D6", "Ouml;"},
        { "00D7", "times;"},
        { "00D8", "Oslash;"},
        { "00D9", "Ugrave;"},
        { "00DA", "Uacute;"},
        { "00DB", "Ucirc;"},
        { "00DC", "Uuml;"},
        { "00DD", "Yacute;"},
        { "00DE", "THORN;"},
        { "00DF", "szlig;"},
        { "00E0", "agrave;"},
        { "00E1", "aacute;"},
        { "00E2", "acirc;"},
        { "00E3", "atilde;"},
        { "00E4", "auml;"},
        { "00E5", "aring;"},
        { "00E6", "aelig;"},
        { "00E7", "ccedil;"},
        { "00E8", "egrave;"},
        { "00E9", "eacute;"},
        { "00EA", "ecirc;"},
        { "00EB", "euml;"},
        { "00EC", "igrave;"},
        { "00ED", "iacute;"},
        { "00EE", "icirc;"},
        { "00EF", "iuml;"},
        { "00F0", "eth;"},
        { "00F1", "ntilde;"},
        { "00F2", "ograve;"},
        { "00F3", "oacute;"},
        { "00F4", "ocirc;"},
        { "00F5", "otilde;"},
        { "00F6", "ouml;"},
        { "00F7", "divide;"},
        { "00F8", "oslash;"},
        { "00F9", "ugrave;"},
        { "00FA", "uacute;"},
        { "00FB", "ucirc;"},
        { "00FC", "uuml;"},
        { "00FD", "yacute;"},
        { "00FE", "thorn;"},
        { "00FF", "yuml;"},
        { "0152", "OElig;"},
        { "0153", "oelig;"},
        { "0160", "Scaron;"},
        { "0161", "scaron;"},
        { "0178", "Yuml;"},
        { "0192", "fnof;"},
        { "02C6", "circ;"},
        { "02DC", "tilde;"},
        { "0391", "Alpha;"},
        { "0392", "Beta;"},
        { "0393", "Gamma;"},
        { "0394", "Delta;"},
        { "0395", "Epsilon;"},
        { "0396", "Zeta;"},
        { "0397", "Eta;"},
        { "0398", "Theta;"},
        { "0399", "Iota;"},
        { "039A", "Kappa;"},
        { "039B", "Lambda;"},
        { "039C", "Mu;"},
        { "039D", "Nu;"},
        { "039E", "Xi;"},
        { "039F", "Omicron;"},
        { "03A0", "Pi;"},
        { "03A1", "Rho;"},
        { "03A3", "Sigma;"},
        { "03A4", "Tau;"},
        { "03A5", "Upsilon;"},
        { "03A6", "Phi;"},
        { "03A7", "Chi;"},
        { "03A8", "Psi;"},
        { "03A9", "Omega;"},
        { "03B1", "alpha;"},
        { "03B2", "beta;"},
        { "03B3", "gamma;"},
        { "03B4", "delta;"},
        { "03B5", "epsilon;"},
        { "03B6", "zeta;"},
        { "03B7", "eta;"},
        { "03B8", "theta;"},
        { "03B9", "iota;"},
        { "03BA", "kappa;"},
        { "03BB", "lambda;"},
        { "03BC", "mu;"},
        { "03BD", "nu;"},
        { "03BE", "xi;"},
        { "03BF", "omicron;"},
        { "03C0", "pi;"},
        { "03C1", "rho;"},
        { "03C2", "sigmaf;"},
        { "03C3", "sigma;"},
        { "03C4", "tau;"},
        { "03C5", "upsilon;"},
        { "03C6", "phi;"},
        { "03C7", "chi;"},
        { "03C8", "psi;"},
        { "03C9", "omega;"},
        { "03D1", "thetasym;"},
        { "03D2", "upsih;"},
        { "03D6", "piv;"},
        { "2002", "ensp;"},
        { "2003", "emsp;"},
        { "2009", "thinsp;"},
        { "200C", "zwnj;"},
        { "200D", "zwj;"},
        { "200E", "lrm;"},
        { "200F", "rlm;"},
        { "2013", "ndash;"},
        { "2014", "mdash;"},
        { "2018", "lsquo;"},
        { "2019", "rsquo;"},
        { "201A", "sbquo;"},
        { "201C", "ldquo;"},
        { "201D", "rdquo;"},
        { "201E", "bdquo;"},
        { "2020", "dagger;"},
        { "2021", "Dagger;"},
        { "2022", "bull;"},
        { "2026", "hellip;"},
        { "2030", "permil;"},
        { "2032", "prime;"},
        { "2033", "Prime;"},
        { "2039", "lsaquo;"},
        { "203A", "rsaquo;"},
        { "203E", "oline;"},
        { "2044", "frasl;"},
        { "20AC", "euro;"},
        { "2111", "image;"},
        { "2118", "weierp;"},
        { "211C", "real;"},
        { "2122", "trade;"},
        { "2135", "alefsym;"},
        { "2190", "larr;"},
        { "2191", "uarr;"},
        { "2192", "rarr;"},
        { "2193", "darr;"},
        { "2194", "harr;"},
        { "21B5", "crarr;"},
        { "21D0", "lArr;"},
        { "21D1", "uArr;"},
        { "21D2", "rArr;"},
        { "21D3", "dArr;"},
        { "21D4", "hArr;"},
        { "2200", "forall;"},
        { "2202", "part;"},
        { "2203", "exist;"},
        { "2205", "empty;"},
        { "2207", "nabla;"},
        { "2208", "isin;"},
        { "2209", "notin;"},
        { "220B", "ni;"},
        { "220F", "prod;"},
        { "2211", "sum;"},
        { "2212", "minus;"},
        { "2217", "lowast;"},
        { "221A", "radic;"},
        { "221D", "prop;"},
        { "221E", "infin;"},
        { "2220", "ang;"},
        { "2227", "and;"},
        { "2228", "or;"},
        { "2229", "cap;"},
        { "222A", "cup;"},
        { "222B", "int;"},
        { "2234", "there4;"},
        { "223C", "sim;"},
        { "2245", "cong;"},
        { "2248", "asymp;"},
        { "2260", "ne;"},
        { "2261", "equiv;"},
        { "2264", "le;"},
        { "2265", "ge;"},
        { "2282", "sub;"},
        { "2283", "sup;"},
        { "2284", "nsub;"},
        { "2286", "sube;"},
        { "2287", "supe;"},
        { "2295", "oplus;"},
        { "2297", "otimes;"},
        { "22A5", "perp;"},
        { "22C5", "sdot;"},
        { "2308", "lceil;"},
        { "2309", "rceil;"},
        { "230A", "lfloor;"},
        { "230B", "rfloor;"},
        { "2329", "lang;"},
        { "232A", "rang;"},
        { "25CA", "loz;"},
        { "2660", "spades;"},
        { "2663", "clubs;"},
        { "2665", "hearts;"},
        { "2666", "diams;"}
    };

    public void eval(ContentHandler responseTarget, ExtensionContext context) 
        throws Exception
    {
        FXContext ctx = extendContext((FXContext)context);

        int exitval = 0;
        String parseable = "";
        String command = (String) ctx.get("command");
        if (command == null || command.length() == 0) {
            command = "ls -al";
        }
        boolean parseOutput = "true".equals((String)ctx.get("parse"));
        
        Runtime r = Runtime.getRuntime();
        try {
            /*
             * Here we are executing the UNIX command ls for directory listing. 
             * The format returned is the long format which includes file 
             * information and permissions.
             */
            Process p = r.exec(command);
            InputStream in = p.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            InputStreamReader inread = new InputStreamReader(buf);
            BufferedReader bufferedreader = new BufferedReader(inread);
            
            // Read the command output
            String line;
            while ((line = bufferedreader.readLine()) != null) {
                parseable += line + "\n";
            }

            try {
                if (p.waitFor() != 0) {
                    exitval = p.exitValue();
                }
            } catch (InterruptedException e) {
                System.err.println(e);
            } finally {
                // Close the InputStream
                bufferedreader.close();
                inread.close();
                buf.close();
                in.close();
            }

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "exit", "exit", "CDATA", "" + exitval);
            startElement("system", atts, responseTarget);

            if (parseOutput) {
                parse(responseTarget, ctx, parseable);
            } else {
                responseTarget.characters(parseable.toCharArray(), 0 , parseable.length());
            }

            endElement("system", responseTarget);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public void parse(ContentHandler responseTarget, FXContext context, String parseable) 
        throws Exception
    {
        // html character entities?
        String htmlChars = (String) context.get("htmlChars");

        // html character entities?
        String asHTML = (String) context.get("asHTML");
        try {
            if ("".equals(parseable)) {
                // do nothing
            
            } else {
                if ("yes".equals(htmlChars)) {
                    parseable = reWriteHTML(parseable);
                }
                InputSource src = new InputSource(new StringReader(parseable));
                src.setSystemId("dummy"); // FIXME: find a better base URI
                
                XMLReader rdr = XForm.createInputSourceReader(src);
                rdr.setContentHandler(responseTarget);
                rdr.parse(src);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorResponse(e, responseTarget, context);
        }
    }

    // rewrite html char entities to unicode
    private String reWriteHTML(String ip)
    {
        
        if (ip == null || ip.length() == 0 || ip.indexOf('&', 0) == -1) {
            return ip;
        }

        String output = "";

        while (ip.length() > 0) {
        
            int next = ip.indexOf('&', 0);
            if (next == -1) {
                output += ip;
                break;
            } else {
                output += ip.substring(0, next);
                ip = ip.substring(next);

                int k = 0;
                for ( ; k < charEntities.length; ++k) {
                    String test = charEntities[k][1];

                    if (ip.length() >  test.length() + 1 &&
                        test.equals(ip.substring(1, test.length() + 1))) {
                        output += "&#x" + charEntities[k][0] + ";";

                        ip = ip.substring(test.length() + 1);
                        break;
                    }
                }
                if (k >= charEntities.length) {
                    // not in *our* list
                    int semi = ip.indexOf(';', 1);
                    if (semi == -1 ) {
                        output += "<error type='unterminated entity ref'>&amp;</error>";
                        ip = ip.substring(1);
                    } else {
                        // let's see the XML parser already knows how to handle this
                        output += testCharEntity("&" + ip.substring(1, semi + 1));
                        ip = ip.substring(semi + 1);
                    }
                }

            }
        }
        return output;
    }

    private String testCharEntity(String entityRef)
    {
        String test = "<z>" + entityRef + "</z>";

        InputSource src = new InputSource(new StringReader(test));
        src.setSystemId("dummy"); // FIXME: find a better base URI

        try {
            XMLReader rdr = XForm.createInputSourceReader(src);
            rdr.setContentHandler(new DefaultHandler());
            rdr.parse(src);
        } catch (Exception ex) {
            //            ex.printStackTrace();
            String rv = "<error type='undefined entity'>&amp;" + 
                entityRef.substring(1) + "</error>";
            return rv;
        }
        return entityRef;
    }
}
