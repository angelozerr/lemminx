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

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Incremental parser that reparses only the affected region of a document
 * when an edit is applied, reusing unchanged subtrees via structural sharing.
 *
 * <p>Given an old {@link GreenDocument}, the new text, and edit coordinates,
 * this class identifies top-level children that are entirely before or after
 * the edit (prefix/suffix), reparses only the affected middle range using
 * {@link GreenTreeBuilder#parseRange}, and splices the result together.</p>
 *
 * <p>Falls back to full reparse when the edit crosses all children or when
 * the reparsed middle ends with an unclosed element (which would invalidate
 * the suffix).</p>
 */
public final class IncrementalParser {

	private IncrementalParser() {
	}

	/**
	 * Incrementally reparses the document. Returns a new {@link GreenDocument}
	 * that structurally shares unchanged subtrees with the old tree.
	 *
	 * @param oldDoc        the previous immutable green tree
	 * @param newText       the full new document text (after edit applied)
	 * @param editStart     offset in old text where the edit begins
	 * @param deleteLength  number of characters deleted from old text
	 * @param insertLength  number of characters inserted (length of new text at edit point)
	 * @param uri           the document URI
	 * @param monitor       optional cancel checker
	 * @return a new GreenDocument (never null)
	 */
	public static GreenDocument incrementalParse(GreenDocument oldDoc,
			String newText, int editStart, int deleteLength, int insertLength,
			String uri, CancelChecker monitor) {
		GreenDocument result = tryIncremental(oldDoc, newText, editStart,
				deleteLength, insertLength, uri, monitor);
		if (result != null) {
			return result;
		}
		return GreenTreeBuilder.parse(newText, uri, monitor);
	}

	private static GreenDocument tryIncremental(GreenDocument oldDoc,
			String newText, int editStart, int deleteLength, int insertLength,
			String uri, CancelChecker monitor) {
		GreenNode[] oldChildren = oldDoc.children();
		if (oldChildren.length == 0) {
			return null;
		}

		int editEnd = editStart + deleteLength;

		// Find reusable prefix: children entirely before the edit
		int prefixCount = 0;
		int prefixWidth = 0;
		for (int i = 0; i < oldChildren.length; i++) {
			int childEnd = prefixWidth + oldChildren[i].width();
			if (childEnd <= editStart) {
				prefixCount++;
				prefixWidth = childEnd;
			} else {
				break;
			}
		}

		// Find reusable suffix: children entirely after the edit in old text
		int suffixCount = 0;
		int suffixWidth = 0;
		int scanPos = oldDoc.width();
		for (int i = oldChildren.length - 1; i >= prefixCount; i--) {
			int childStart = scanPos - oldChildren[i].width();
			if (childStart >= editEnd) {
				suffixCount++;
				suffixWidth += oldChildren[i].width();
				scanPos = childStart;
			} else {
				break;
			}
		}

		if (prefixCount + suffixCount == 0) {
			return null;
		}

		int middleStart = prefixWidth;
		int middleEnd = newText.length() - suffixWidth;
		if (middleEnd < middleStart || middleEnd > newText.length()) {
			return null;
		}

		GreenDocument middleDoc = GreenTreeBuilder.parseRange(
				newText, uri, middleStart, middleEnd, monitor);
		GreenNode[] middleChildren = middleDoc.children();

		if (suffixCount > 0 && middleChildren.length > 0
				&& !middleChildren[middleChildren.length - 1].closed()) {
			middleDoc = GreenTreeBuilder.parseRange(
					newText, uri, middleStart, newText.length(), monitor);
			middleChildren = middleDoc.children();
			suffixCount = 0;
			suffixWidth = 0;
		}

		int total = prefixCount + middleChildren.length + suffixCount;
		GreenNode[] newChildren = new GreenNode[total];
		System.arraycopy(oldChildren, 0, newChildren, 0, prefixCount);
		System.arraycopy(middleChildren, 0, newChildren, prefixCount,
				middleChildren.length);
		if (suffixCount > 0) {
			System.arraycopy(oldChildren, oldChildren.length - suffixCount,
					newChildren, prefixCount + middleChildren.length, suffixCount);
		}

		return new GreenDocument(newText.length(), newChildren);
	}
}
