/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.performance;

import static org.eclipse.lemminx.utils.IOUtils.convertStreamToString;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

/**
 * This utility class is used to check the memory usage of {@link DOMParser},
 * loading the large content.xml file.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMParserPerformance2 {

	public static void main(String[] args) {
		InputStream in = DOMParserPerformance2.class.getResourceAsStream("/xml/content.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "content.xml");
		document.setIncremental(true);
		// Continuously parses the large content.xml file with the DOM parser.
		while (true) {
			long start = System.currentTimeMillis();
			
			
			List<TextDocumentContentChangeEvent> changes = new ArrayList<>();
			TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
					new Range(new Position(0, 0), new Position(0, 0)), 0, " ");
			changes.add(change);
			document.update(changes);
			System.err.println("Updated 'content.xml' with DOMParser in " + (System.currentTimeMillis() - start) + " ms.");
			
			
			start = System.currentTimeMillis();
			DOMDocument xmlDocument = DOMParser.getInstance().parse(document, null);
			
			//xmlDocument.dispose();
			System.err.println("Parsed 'content.xml' with DOMParser in " + (System.currentTimeMillis() - start) + " ms.");
		}
	}
}
