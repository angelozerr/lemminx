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

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable builder for {@link GreenElement}.
 *
 * <p>Used during parsing: the scanner populates fields across multiple
 * tokens, then {@link #build()} freezes the result into an immutable
 * {@link GreenElement}.</p>
 */
public final class GreenElementBuilder {

	private final int nodeStart;
	private int nodeEnd;
	private String tag;
	private boolean selfClosed;
	private boolean closed;
	private int startTagCloseOffset = GreenElement.NULL_VALUE;
	private int endTagOpenOffset = GreenElement.NULL_VALUE;
	private int endTagCloseOffset = GreenElement.NULL_VALUE;
	private List<GreenAttr> attributes;
	private List<GreenNode> children;

	public GreenElementBuilder(int nodeStart) {
		this.nodeStart = nodeStart;
		this.nodeEnd = nodeStart;
	}

	public int nodeStart() {
		return nodeStart;
	}

	public int nodeEnd() {
		return nodeEnd;
	}

	public void setNodeEnd(int nodeEnd) {
		this.nodeEnd = nodeEnd;
	}

	public String tag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setSelfClosed(boolean selfClosed) {
		this.selfClosed = selfClosed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public void setStartTagCloseOffset(int offset) {
		this.startTagCloseOffset = offset;
	}

	public void setEndTagOpenOffset(int offset) {
		this.endTagOpenOffset = offset;
	}

	public void setEndTagCloseOffset(int offset) {
		this.endTagCloseOffset = offset;
	}

	public void addAttribute(GreenAttr attr) {
		if (attributes == null) {
			attributes = new ArrayList<>(4);
		}
		attributes.add(attr);
	}

	public void addChild(GreenNode child) {
		if (children == null) {
			children = new ArrayList<>(4);
		}
		children.add(child);
	}

	public GreenElement build() {
		int width = nodeEnd - nodeStart;
		int stcRel = startTagCloseOffset != GreenElement.NULL_VALUE
				? startTagCloseOffset - nodeStart : GreenElement.NULL_VALUE;
		int etoRel = endTagOpenOffset != GreenElement.NULL_VALUE
				? endTagOpenOffset - nodeStart : GreenElement.NULL_VALUE;
		int etcRel = endTagCloseOffset != GreenElement.NULL_VALUE
				? endTagCloseOffset - nodeStart : GreenElement.NULL_VALUE;

		GreenAttr[] attrs = attributes != null
				? attributes.toArray(GreenNode.EMPTY_ATTRS) : null;
		GreenNode[] kids = children != null
				? children.toArray(GreenNode.EMPTY_CHILDREN) : null;

		return new GreenElement(width, closed, tag, selfClosed,
				stcRel, etoRel, etcRel, GreenElement.NULL_VALUE, attrs, kids);
	}
}
