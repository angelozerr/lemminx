/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lemminx.dom.green;

import org.w3c.dom.Node;

/**
 * Immutable green node for an XML element.
 *
 * <p>All offsets are <em>relative to the node's own start</em> (i.e., relative
 * offset 0 is the first character of the element). The red wrapper adds the
 * absolute document offset when queried.</p>
 */
public final class GreenElement extends GreenNode {

	public static final int NULL_VALUE = -1;

	private final String tag;
	private final int startTagCloseRel;
	private final int endTagOpenRel;
	private final int endTagCloseRel;
	private final GreenAttr[] attributes;
	private final GreenNode[] children;

	public GreenElement(int width, boolean closed, String tag, boolean selfClosed,
			int startTagCloseRel, int endTagOpenRel, int endTagCloseRel,
			GreenAttr[] attributes, GreenNode[] children) {
		super(width, (closed ? CLOSED_FLAG : 0) | (selfClosed ? SUBCLASS_FLAG : 0));
		this.tag = tag;
		this.startTagCloseRel = startTagCloseRel;
		this.endTagOpenRel = endTagOpenRel;
		this.endTagCloseRel = endTagCloseRel;
		this.attributes = attributes != null ? attributes : EMPTY_ATTRS;
		this.children = children != null ? children : EMPTY_CHILDREN;
	}

	@Override
	public short nodeType() {
		return Node.ELEMENT_NODE;
	}

	public String tag() {
		return tag;
	}

	public boolean selfClosed() {
		return subclassFlag();
	}

	public int startTagCloseRel() {
		return startTagCloseRel;
	}

	public int endTagOpenRel() {
		return endTagOpenRel;
	}

	public int endTagCloseRel() {
		return endTagCloseRel;
	}

	public GreenAttr[] attributes() {
		return attributes;
	}

	public int attributeCount() {
		return attributes.length;
	}

	@Override
	public int childrenStartRel() {
		if (startTagCloseRel != NULL_VALUE) {
			return startTagCloseRel + 1;
		}
		return 0;
	}

	@Override
	public GreenNode[] children() {
		return children;
	}

	public GreenElement withNewChildren(GreenNode[] newChildren, int widthDelta) {
		return new GreenElement(
				width() + widthDelta, closed(), tag, selfClosed(),
				startTagCloseRel,
				endTagOpenRel != NULL_VALUE ? endTagOpenRel + widthDelta : NULL_VALUE,
				endTagCloseRel != NULL_VALUE ? endTagCloseRel + widthDelta : NULL_VALUE,
				attributes, newChildren);
	}

	@Override
	protected GreenNode replaceChildren(GreenNode[] newChildren, int newWidth) {
		return new GreenElement(newWidth, closed(), tag, selfClosed(),
				startTagCloseRel, endTagOpenRel, endTagCloseRel,
				attributes, newChildren);
	}
}
