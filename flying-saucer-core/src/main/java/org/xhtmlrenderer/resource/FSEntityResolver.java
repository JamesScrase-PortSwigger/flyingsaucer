/*
 * {{{ header & license
 * FSEntityResolver.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.resource;

import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * <p>
 * A SAX EntityResolver for common entity references and DTDs in X/HTML
 * processing. Maps official entity references to local copies to avoid network
 * lookup. The local copies are stored in the source tree under /entities, and
 * the references here are resolved by a system ClassLoader. As long as the
 * entity files are in the classpath (or bundled in the FS jar), they will be
 * picked up.
 * </p>
 * <p>
 * The basic form of this class comes from Elliot Rusty Harold, on
 * <a href="http://www.cafeconleche.org/books/xmljava/chapters/ch07s02.html">...</a>
 * </p>
 * <p>
 * This class is a Singleton; use {@link #instance} to retrieve it.
 * </p>
 *
 * @author Patrick Wright
 */
public class FSEntityResolver implements EntityResolver2 {
    /**
     * Singleton instance, use {@link #instance()} to retrieve.
     */
    private static FSEntityResolver instance;

    private final Map<String, String> entities = new HashMap<>();

    // fill the list with URLs
    private FSEntityResolver() {
        FSCatalog catalog = new FSCatalog();

        // The HTML 4.01 DTDs; includes entities. Load from catalog file.
        entities.putAll(catalog.parseCatalog("resources/schema/html-4.01/catalog-html-4.01.xml"));

        // XHTML common (shared declarations)
        entities.putAll(catalog.parseCatalog("resources/schema/xhtml/catalog-xhtml-common.xml"));

        // The XHTML 1.0 DTDs
        entities.putAll(catalog.parseCatalog("resources/schema/xhtml/catalog-xhtml-1.0.xml"));

        // The XHMTL 1.1 DTD
        entities.putAll(catalog.parseCatalog("resources/schema/xhtml/catalog-xhtml-1.1.xml"));

        // DocBook DTDs
        entities.putAll(catalog.parseCatalog("resources/schema/docbook/catalog-docbook.xml"));

        // The XHTML 1.1 element sets
    }

    @Override
    public InputSource resolveEntity(String publicID, String systemID) {

        InputSource local = null;
        String url = getEntity(publicID);
        if (url != null) {
            URL realUrl = GeneralUtil.getURLFromClasspath(this, url);
            InputStream is = null;
            try {
                is = realUrl.openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (is == null) {
                XRLog.xmlEntities(Level.WARNING,
                        "Can't find a local reference for Entity for public ID: " + publicID +
                        " and expected to. The local URL should be: " + url + ". Not finding " +
                        "this probably means a CLASSPATH configuration problem; this resource " +
                        "should be included with the renderer and so not finding it means it is " +
                        "not on the CLASSPATH, and should be. Will let parser use the default in " +
                        "this case.");
            }
            local = new InputSource(is);
            local.setSystemId(realUrl.toExternalForm());
            XRLog.xmlEntities(Level.FINE, "Entity public: " + publicID + " -> " + url + " (local)");
        } else if ("about:legacy-compat".equals(systemID)) {
            // https://www.w3.org/TR/html5/syntax.html#doctype-legacy-string
            // https://www.w3.org/TR/html51/syntax.html#doctype-legacy-string
            local = newHTML5DoctypeSource();
        } else {
            XRLog.xmlEntities("Entity public: " + publicID + ", no local mapping. Replacing with empty content.");
        }
        return (local == null) ? newEmptySource() : local;
    }

    @Override
    public InputSource resolveEntity(String name,
                                     String publicId,
                                     String baseURI,
                                     String systemId) {
        return resolveEntity(publicId, systemId);
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) {
        return name.equalsIgnoreCase("html") ? newHTML5DoctypeSource() : null;
    }

    private static InputSource newHTML5DoctypeSource() {
        URL dtd = FSEntityResolver.class
                .getResource("/resources/schema/html5/entities.dtd");
        if (dtd == null) {
            throw new IllegalStateException("Could not find " +
                    "/resources/schema/html5/entities.dtd on the classpath");
        }
        return new InputSource(dtd.toExternalForm());
    }

    private static InputSource newEmptySource() {
        return new InputSource(new StringReader(""));
    }

    /**
     * Gets an instance of this class.
     *
     * @return An instance of .
     */
    public static synchronized FSEntityResolver instance() {
        if (instance == null) {
            instance = new FSEntityResolver();
        }
        return instance;
    }

    /**
     * Returns a map of entities parsed by this resolver.
     */
    public Map<String, String> getEntities() {
        return new HashMap<>(entities);
    }
    
    public String getEntity(String url) {
        return entities.get(url);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.8  2008/12/01 20:37:24  pdoubleya
 * Expose copy of parsed entities from catalog.
 *
 * Revision 1.7  2007/05/21 22:13:02  peterbrant
 * Code cleanup (patch from Sean Bright)
 *
 * Revision 1.6  2007/05/20 23:25:34  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.5  2005/06/13 06:50:15  tobega
 * Fixed a bug in table content resolution.
 * Various "tweaks" in other stuff.
 *
 * Revision 1.4  2005/03/28 14:24:48  pdoubleya
 * Changed to resolve all entities using simple catalog files.
 *
 * Revision 1.3  2005/03/27 18:36:26  pdoubleya
 * Added separate logging for entity resolution.
 *
 * Revision 1.2  2005/03/21 09:13:50  pdoubleya
 * Added XHTML 1.1 references (Kevin).
 *
 * Revision 1.1  2005/02/03 20:39:34  pdoubleya
 * Added to CVS.
 *
 *
 */

