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
package org.eclipse.lemminx.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModelTextDocument} incremental edit support.
 */
public class ModelTextDocumentTest {

	@Test
	public void editInfoCapturedBeforeTextUpdate() {
		ModelTextDocument<String> doc = createDoc("<root>text</root>");
		doc.getModel();

		TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
				new Range(new Position(0, 10), new Position(0, 10)), "X");
		doc.update(Collections.singletonList(change));

		ModelTextDocument.EditInfo edit = doc.getPendingEdit();
		assertNotNull(edit);
		assertEquals(10, edit.getStartOffset());
		assertEquals(0, edit.getDeleteLength());
		assertEquals(1, edit.getInsertLength());
	}

	@Test
	public void previousModelPreservedOnCancel() {
		ModelTextDocument<String> doc = createDoc("<root/>");
		String firstModel = doc.getModel();
		assertNotNull(firstModel);

		doc.setText("<root>changed</root>");

		String prev = doc.getPreviousModel();
		assertNotNull(prev);
		assertEquals(firstModel, prev);
	}

	@Test
	public void pendingEditClearedAfterGetModel() {
		ModelTextDocument<String> doc = createDoc("<root>text</root>");
		doc.getModel();

		TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
				new Range(new Position(0, 10), new Position(0, 10)), "X");
		doc.update(Collections.singletonList(change));
		assertNotNull(doc.getPendingEdit());

		doc.getModel();

		assertNull(doc.getPendingEdit());
		assertNull(doc.getPreviousModel());
	}

	@Test
	public void multipleChangesNullifyPendingEdit() {
		ModelTextDocument<String> doc = createDoc("<root>text</root>");
		doc.getModel();

		TextDocumentContentChangeEvent change1 = createChange(0, 6, 0, 10, "newtext");
		TextDocumentContentChangeEvent change2 = createChange(0, 0, 0, 0, "X");
		doc.update(Arrays.asList(change1, change2));

		assertNull(doc.getPendingEdit());
	}

	@Test
	public void fullDocumentChangeNullifyPendingEdit() {
		ModelTextDocument<String> doc = createDoc("<root/>");
		doc.getModel();

		TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent("<new/>");
		doc.update(Collections.singletonList(change));

		assertNull(doc.getPendingEdit());
	}

	@Test
	public void doubleCancelModelPreservesFirstPreviousModel() {
		ModelTextDocument<String> doc = createDoc("<root/>");
		String firstModel = doc.getModel();
		assertNotNull(firstModel);

		doc.setText("<root>changed1</root>");
		String prevAfterFirst = doc.getPreviousModel();
		assertEquals(firstModel, prevAfterFirst);

		// Second setText without getModel() in between — model is already null,
		// so cancelModel() does NOT overwrite previousModel
		doc.setText("<root>changed2</root>");
		String prevAfterSecond = doc.getPreviousModel();
		assertEquals(firstModel, prevAfterSecond);
	}

	private ModelTextDocument<String> createDoc(String text) {
		ModelTextDocument<String> doc = new ModelTextDocument<>(text, "test://test.xml",
				(document, cancelChecker) -> document.getText());
		doc.setIncremental(true);
		return doc;
	}

	private TextDocumentContentChangeEvent createChange(
			int startLine, int startChar, int endLine, int endChar, String text) {
		return new TextDocumentContentChangeEvent(
				new Range(new Position(startLine, startChar), new Position(endLine, endChar)),
				text);
	}
}
