/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.le;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testLinkedEditingFor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lemminx.MockXMLLanguageServer;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.settings.AllXMLSettings;
import org.eclipse.lemminx.utils.platform.Platform;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for XML linked editing.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLLinkedEditingTest {

	@Test
	public void linkedEditingWithOrpheanEndTag() throws BadLocationException {
		testLinkedEditingFor("<div></|", null);
		testLinkedEditingFor("<di|v></", null);
		testLinkedEditingFor("<|></", null);
	}

	@Test
	public void linkedEditing() throws BadLocationException {
		testLinkedEditingFor("|<div></div>", null);
		testLinkedEditingFor("<|div></div>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<d|iv></div>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<di|v></div>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div|></div>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));

		testLinkedEditingFor("<div>|</div>", null);
		testLinkedEditingFor("<div><|/div>", null);

		testLinkedEditingFor("<div></|div>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></d|iv>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></di|v>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></div|>", le(r(0, 1, 0, 4), r(0, 7, 0, 10)));

		testLinkedEditingFor("<div></div>|", null);
		testLinkedEditingFor("<div><div|</div>", le(r(0, 6, 0, 9), r(0, 11, 0, 14)));
		testLinkedEditingFor("<div><div><div|</div></div>", le(r(0, 11, 0, 14), r(0, 16, 0, 19)));

		testLinkedEditingFor("<div| ></div>", le(r(0, 1, 0, 4), r(0, 8, 0, 11)));
		testLinkedEditingFor("<div| id='foo'></div>", le(r(0, 1, 0, 4), r(0, 16, 0, 19)));
	}

	@Test
	public void noDiagnosticWithLinkedEditing() throws InterruptedException, ExecutionException {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		AllXMLSettings allSettings = new AllXMLSettings();
		ContentModelSettings xmlSettings = new ContentModelSettings();
		allSettings.setXml(xmlSettings);
		XMLValidationSettings validation = new XMLValidationSettings();
		validation.setNoGrammar("ignore");
		xmlSettings.setValidation(validation);
		languageServer.updateSettings(allSettings);

		String xml = "<foo></foo>";
		String xmlPath = getFileURI("src/test/resources/tag.xml");
		languageServer.didOpen(xmlPath, xml);
		Thread.sleep(200);
		
		// Add '1' after <foo
		// --> <foo1>
		languageServer.didChange(xmlPath, Arrays.asList(new TextDocumentContentChangeEvent(r(0, 4, 0, 4), 0, "1")));
		// Emulate linked editing range to update </foo>
		// --> </foo1>
		Thread.sleep(200);
		System.err.println("2 didChange");
		languageServer.didChange(xmlPath, Arrays.asList(new TextDocumentContentChangeEvent(r(0, 11, 0, 11), 0, "1")));
		
		List<PublishDiagnosticsParams> publishDiagnostics = languageServer.getPublishDiagnostics();
		Thread.sleep(5000);
		
		Assertions.assertEquals(1, publishDiagnostics.size());
		Assertions.assertEquals(0, publishDiagnostics.get(0).getDiagnostics().size());
	}

	private static String getFileURI(String fileName) {
		String uri = new File(fileName).toURI().toString();
		if (Platform.isWindows && !uri.startsWith("file://")) {
			uri = uri.replace("file:/", "file:///");
		}
		return uri;
	}
}
