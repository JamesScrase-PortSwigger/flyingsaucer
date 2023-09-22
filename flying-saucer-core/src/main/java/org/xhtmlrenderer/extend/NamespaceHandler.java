/*
 * Document.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */
package org.xhtmlrenderer.extend;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;

import javax.annotation.Nullable;

/**
 * Provides knowledge specific to a certain document type, like resolving
 * style-sheets
 *
 * @author Torbjoern Gannholm
 */
public interface NamespaceHandler {

    /**
     * @return the namespace handled
     */
    String getNamespace();

    /**
     * @return the default CSS stylesheet for this namespace
     */
    StylesheetInfo getDefaultStylesheet(StylesheetFactory factory);

    /**
     * @param doc
     * @return the title for this document, if any exists
     */
    String getDocumentTitle(Document doc);

    /**
     * @param doc
     * @return all links to CSS stylesheets (type="text/css") in this
     *         document
     */
    StylesheetInfo[] getStylesheets(Document doc);

    /**
     * may return null. Required to return null if attribute does not exist and
     * not null if attribute exists.
     */
    String getAttributeValue(org.w3c.dom.Element e, String attrName);

    String getAttributeValue(org.w3c.dom.Element e, String namespaceURI, String attrName);

    @Nullable
    String getClass(org.w3c.dom.Element e);

    @Nullable
    String getID(org.w3c.dom.Element e);

    @Nullable
    String getElementStyling(org.w3c.dom.Element e);

    /**
     * @return The corresponding css properties for styling that is obtained in other ways.
     */
    @Nullable
    String getNonCssStyling(org.w3c.dom.Element e);

    @Nullable
    String getLang(org.w3c.dom.Element e);

    /**
     * should return null if element is not a link
     */
    @Nullable
    String getLinkUri(org.w3c.dom.Element e);

    String getAnchorName(Element e);

    /**
     * @return Returns true if the Element represents an image.
     */
    boolean isImageElement(Element e);

    /**
     * Determines whether the specified Element represents a
     * &lt;form&gt;.
     *
     * @param e The Element to evaluate.
     * @return true if the Element is a &lt;form&gt; element, false otherwise.
     */
    boolean isFormElement(Element e);

    /**
     * For an element where isImageElement returns true, retrieves the URI associated with that Image, as
     * reported by the element; makes no guarantee that the URI is correct, complete or points to anything in
     * particular. For elements where {@link #isImageElement(org.w3c.dom.Element)} returns false, this method may
     * return false, and may also return false if the Element is not correctly formed and contains no URI; check the
     * return value carefully.
     *
     * @param e The element to extract image info from.
     * @return String containing the URI for the image.
     */
    String getImageSourceURI(Element e);
}

