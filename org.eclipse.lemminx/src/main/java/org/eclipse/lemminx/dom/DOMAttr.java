/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.dom;

import java.util.List;
import java.util.Objects;

import org.eclipse.lemminx.utils.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * An attribute node.
 *
 */
public class DOMAttr extends DOMNode implements org.w3c.dom.Attr {

	private final String name;

	private final AttrNameOrValue nodeAttrName;

	private AttrNameOrValue nodeAttrValue;

	private final DOMNode ownerElement;

	private boolean hasDelimiter; // has '='

	private String value;

	public static final String XMLNS_ATTR = "xmlns";
	public static final String XMLNS_NO_DEFAULT_ATTR = "xmlns:";

	private static class AttrNameOrValue implements DOMRange {

		private final int start;

		private final int end;

		private final DOMAttr ownerAttr;

		private String content;

		private String lessContent;

		public AttrNameOrValue(int start, int end, DOMAttr ownerAttr) {
			this.start = start;
			this.end = end;
			this.ownerAttr = ownerAttr;
		}

		@Override
		public int getStart() {
			return start;
		}

		@Override
		public int getEnd() {
			return end;
		}

		public DOMAttr getOwnerAttr() {
			return ownerAttr;
		}

		@Override
		public DOMDocument getOwnerDocument() {
			return getOwnerAttr().getOwnerDocument();
		}

		public String getContent() {
			if (content == null) {
				content = getOwnerDocument().getTextDocument().getText().substring(getStart(), getEnd());
			}
			return content;
		}

		public String getQuotelessContent() {
			if (lessContent == null) {
				lessContent = StringUtils.convertToQuotelessValue(getContent());
			}
			return lessContent;
		}

		@Override
		public int hashCode() {
			return Objects.hash(end, ownerAttr, start);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttrNameOrValue other = (AttrNameOrValue) obj;
			return end == other.end && Objects.equals(ownerAttr, other.ownerAttr) && start == other.start;
		}

	}

	public DOMAttr(String name, DOMNode ownerElement) {
		this(name, NULL_VALUE, NULL_VALUE, ownerElement);
	}

	public DOMAttr(int start, int end, DOMNode ownerElement) {
		this(null, start, end, ownerElement);
	}

	private DOMAttr(String name, int start, int end, DOMNode ownerElement) {
		super(NULL_VALUE, NULL_VALUE);
		this.name = name;
		this.nodeAttrName = start != NULL_VALUE ? new AttrNameOrValue(start, end, this) : null;
		this.ownerElement = ownerElement;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return DOMNode.ATTRIBUTE_NODE;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#getName()
	 */
	@Override
	public String getName() {
		if (name != null) {
			return name;
		}
		return nodeAttrName != null ? nodeAttrName.getContent() : "";
	}

	@Override
	public String getNodeValue() throws DOMException {
		return getValue();
	}

	@Override
	public String getLocalName() {
		String name = getName();
		int colonIndex = name.indexOf(":");
		if (colonIndex > 0) {
			return name.substring(colonIndex + 1);
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#getOwnerElement()
	 */
	public DOMElement getOwnerElement() {
		return ownerElement.isElement() ? (DOMElement) ownerElement : null;
	}

	@Override
	public DOMDocument getOwnerDocument() {
		return ownerElement != null ? ownerElement.getOwnerDocument() : null;
	}

	/*
	 *
	 * Returns the attribute's value without quotes.
	 */
	@Override
	public String getValue() {
		if (value != null) {
			return value;
		}
		return nodeAttrValue != null ? nodeAttrValue.getQuotelessContent() : "";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#getSchemaTypeInfo()
	 */
	@Override
	public TypeInfo getSchemaTypeInfo() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#getSpecified()
	 */
	@Override
	public boolean getSpecified() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#isId()
	 */
	@Override
	public boolean isId() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Attr#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) throws DOMException {
		this.value = value;
	}

	public DOMRange getNodeAttrName() {
		return nodeAttrName;
	}

	public void setDelimiter(boolean hasDelimiter) {
		this.hasDelimiter = hasDelimiter;
	}

	public boolean hasDelimiter() {
		return this.hasDelimiter;
	}

	/**
	 * Get original attribute value from the document.
	 *
	 * This will include quotations (", ').
	 *
	 * @return attribute value with quotations if it had them.
	 */
	public String getOriginalValue() {
		return nodeAttrValue != null ? nodeAttrValue.getContent() : null;
	}

	public void setValue(int start, int end) {
		this.nodeAttrValue = start != -1 ? new AttrNameOrValue(start, end, this) : null;
	}

	public DOMRange getNodeAttrValue() {
		return nodeAttrValue;
	}

	public boolean valueContainsOffset(int offset) {
		return nodeAttrValue != null && offset >= nodeAttrValue.getStart() && offset < nodeAttrValue.getEnd();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	@Override
	public String getPrefix() {
		String name = getName();
		if (name == null) {
			return null;
		}
		String prefix = null;
		int index = name.indexOf(":"); //$NON-NLS-1$
		if (index != -1) {
			prefix = name.substring(0, index);
		}
		return prefix;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	@Override
	public String getNamespaceURI() {
		if (ownerElement == null || ownerElement.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
		String prefix = getPrefix();
		// Try to get xmlns attribute from the element
		return ((DOMElement) ownerElement).getNamespaceURI(prefix);
	}

	/**
	 * Returns true if attribute name is a xmlns attribute and false otherwise.
	 *
	 * @param attributeName
	 * @return true if attribute name is a xmlns attribute and false otherwise.
	 */
	public boolean isXmlns() {
		return isXmlns(getName());
	}

	public static boolean isXmlns(String attributeName) {
		return attributeName.startsWith(XMLNS_ATTR);
	}

	/**
	 * Returns true if attribute name is the default xmlns attribute and false
	 * otherwise.
	 *
	 * @param attributeName
	 * @return true if attribute name is the default xmlns attribute and false
	 *         otherwise.
	 */
	public boolean isDefaultXmlns() {
		return isDefaultXmlns(getName());
	}

	public static boolean isDefaultXmlns(String attributeName) {
		return attributeName.equals(XMLNS_ATTR);
	}

	public String extractPrefixFromXmlns() {
		String name = getName();
		if (isDefaultXmlns()) {
			return name.substring(XMLNS_ATTR.length(), name.length());
		}
		return name.substring(XMLNS_NO_DEFAULT_ATTR.length(), name.length());
	}

	/**
	 * Returns the prefix if the given URI matches this attributes value.
	 *
	 * If the URI doesnt match, null is returned.
	 *
	 * @param uri
	 * @return
	 */
	public String getPrefixIfMatchesURI(String uri) {
		if (isXmlns()) {
			String quotelessValue = getValue();
			if (quotelessValue != null && quotelessValue.equals(uri)) {
				if (isDefaultXmlns()) {
					// xmlns="http://"
					return null;
				}
				// xmlns:xxx="http://"
				return extractPrefixFromXmlns();
			}
		}
		return null;
	}

	/**
	 * Returns true if attribute name is the no default xmlns attribute and false
	 * otherwise.
	 *
	 * @param attributeName
	 * @return true if attribute name is the no default xmlns attribute and false
	 *         otherwise.
	 */
	public boolean isNoDefaultXmlns() {
		return isNoDefaultXmlns(getName());
	}

	public static boolean isNoDefaultXmlns(String attributeName) {
		return attributeName.startsWith(XMLNS_NO_DEFAULT_ATTR);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.Node#getNextSibling()
	 */
	@Override
	public DOMNode getNextSibling() {
		DOMNode parentNode = getOwnerElement();
		if (parentNode == null) {
			return null;
		}
		List<DOMAttr> children = parentNode.getAttributeNodes();
		int nextIndex = children.indexOf(this) + 1;
		return nextIndex < children.size() ? children.get(nextIndex) : null;
	}

	public boolean isIncluded(int offset) {
		return DOMNode.isIncluded(getStart(), getEnd(), offset);
	}

	@Override
	public int getStart() {
		return nodeAttrName != null ? nodeAttrName.getStart() : NULL_VALUE;
	}

	@Override
	public int getEnd() {
		return nodeAttrValue != null ? nodeAttrValue.getEnd()
				: (nodeAttrName != null ? nodeAttrName.getEnd() : NULL_VALUE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeAttrName, nodeAttrValue, ownerElement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DOMAttr other = (DOMAttr) obj;
		return Objects.equals(getName(), other.getName()) && Objects.equals(getValue(), other.getValue());
	}

}
