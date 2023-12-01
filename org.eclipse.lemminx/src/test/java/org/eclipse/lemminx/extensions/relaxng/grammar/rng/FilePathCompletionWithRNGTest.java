/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.filepath.participants.AbstractFilePathCompletionTest;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * File path support completion test with
 * 
 * <ul>
 * <li>include/@href</li>
 * <li>externalRef/@href</li>
 * </ul>
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithRNGTest extends AbstractFilePathCompletionTest {

	@Test
	public void includeHref() throws BadLocationException {
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"\n"
				+ "	datatypeLibrary=\"http://www.w3.org/2001/XMLSchema-datatypes\">\n"
				+ "	<include href=\"|\" />\n"
				+ "</grammar>";
		CompletionItem[] items = getCompletionItemList(2, 16, 16, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd", "main.dtd", "main.xml", "main.xsl");
		testCompletionFor(xml, 8, items);
	}

	@Test
	public void externalRefHref() throws BadLocationException {
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"\n"
				+ "	datatypeLibrary=\"http://www.w3.org/2001/XMLSchema-datatypes\">\n"
				+ "	<externalRef href=\"|\" />\n"
				+ "</grammar>";
		CompletionItem[] items = getCompletionItemList(2, 20, 20, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd", "main.dtd", "main.xml", "main.xsl");
		testCompletionFor(xml, 8, items);
	}
}