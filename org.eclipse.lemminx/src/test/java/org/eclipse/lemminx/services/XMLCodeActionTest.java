/**
 *  Copyright (c) 2024 Angelo ZERR
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
package org.eclipse.lemminx.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.MockXMLLanguageServer;
import org.eclipse.lemminx.XMLTextDocumentService;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XMLTextDocumentService#codeAction}.
 *
 * @see <a href="https://github.com/eclipse-lemminx/lemminx/issues/1603">issue
 *      #1603</a>
 */
public class XMLCodeActionTest extends AbstractCacheBasedTest {

	@Test
	public void noNPEWhenCodeActionLiteralSupportIsFalse() {
		MockXMLLanguageServer server = new MockXMLLanguageServer();

		String uri = "file:///test.xml";
		server.didOpen(uri, "<root></root>");

		CodeActionParams params = createCodeActionParams(uri);

		List<Either<Command, CodeAction>> result = assertDoesNotThrow(() -> {
			return server.getTextDocumentService().codeAction(params).join();
		});
		assertNotNull(result);
	}

	@Test
	public void noNPEWhenResolveSupportWithoutLiteralSupport() {
		MockXMLLanguageServer server = new MockXMLLanguageServer();

		ClientCapabilities capabilities = new ClientCapabilities();
		TextDocumentClientCapabilities textDocCaps = new TextDocumentClientCapabilities();
		CodeActionCapabilities codeActionCaps = new CodeActionCapabilities();
		codeActionCaps.setResolveSupport(
				new CodeActionResolveSupportCapabilities(Arrays.asList("edit")));
		textDocCaps.setCodeAction(codeActionCaps);
		capabilities.setTextDocument(textDocCaps);
		((XMLTextDocumentService) server.getTextDocumentService()).updateClientCapabilities(capabilities, null);

		String uri = "file:///test.xml";
		server.didOpen(uri, "<root></root>");

		CodeActionParams params = createCodeActionParams(uri);

		List<Either<Command, CodeAction>> result = assertDoesNotThrow(() -> {
			return server.getTextDocumentService().codeAction(params).join();
		});
		assertNotNull(result);
	}

	private static CodeActionParams createCodeActionParams(String uri) {
		CodeActionParams params = new CodeActionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setRange(new Range(new Position(0, 0), new Position(0, 13)));
		CodeActionContext context = new CodeActionContext();
		Diagnostic diagnostic = new Diagnostic(
				new Range(new Position(0, 1), new Position(0, 5)),
				"No grammar constraints (document type) detected.",
				DiagnosticSeverity.Hint,
				"xml",
				"NoGrammarConstraints");
		context.setDiagnostics(Arrays.asList(diagnostic));
		params.setContext(context);
		return params;
	}
}
