/**
 * Copyright (c) 2020 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.dom;

import java.util.List;

import org.w3c.dom.NodeList;

public abstract class DOMParentNode extends DOMNode {

	private XMLNodeList<DOMNode> children;

	public DOMParentNode(int start, int end) {
		super(start, end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	@Override
	public boolean hasChildNodes() {
		return children != null && !children.isEmpty();
	}

	@Override
	public NodeList getChildNodes() {
		return children != null ? children : super.getChildNodes();
	}

	@Override
	public List<DOMNode> getChildren() {
		return children != null ? children : super.getChildren();
	}

	@Override
	public void addChild(DOMNode child) {
		child.parent = this;
		if (children == null) {
			children = new XMLNodeList<>();
		}
		getChildren().add(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
	@Override
	public DOMNode getFirstChild() {
		return this.children != null && children.size() > 0 ? this.children.get(0) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getLastChild()
	 */
	@Override
	public DOMNode getLastChild() {
		return this.children != null && this.children.size() > 0 ? this.children.get(this.children.size() - 1) : null;
	}
}
