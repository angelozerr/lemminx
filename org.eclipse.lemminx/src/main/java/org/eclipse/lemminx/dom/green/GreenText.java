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
 * Immutable green node for a text content node.
 */
public final class GreenText extends GreenNode {

	private final boolean whitespace;

	public GreenText(int width, boolean whitespace) {
		super(width, true);
		this.whitespace = whitespace;
	}

	@Override
	public short nodeType() {
		return Node.TEXT_NODE;
	}

	public boolean whitespace() {
		return whitespace;
	}

	@Override
	protected GreenNode replaceChildren(GreenNode[] newChildren, int newWidth) {
		return new GreenText(newWidth, whitespace);
	}
}
