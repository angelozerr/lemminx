package org.eclipse.lemminx.services.extensions.commands;

import java.util.Map;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.IXMLCommandService.IDelegateCommandHandler;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.utils.JSONUtility;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public abstract class AbstractDOMDocumentCommandHandler implements IDelegateCommandHandler {

	private final IXMLDocumentProvider documentProvider;

	public AbstractDOMDocumentCommandHandler(IXMLDocumentProvider documentProvider) {
		this.documentProvider = documentProvider;
	}

	@Override
	public final Object executeCommand(ExecuteCommandParams params, CancelChecker cancelChecker) throws Exception {
		String uri = null;
		TextDocumentIdentifier identifier = JSONUtility.toModel(params.getArguments().get(0), TextDocumentIdentifier.class);
		if (identifier == null) {
			
		} else {
			uri = identifier.getUri();
		}
		if (uri == null) {

		}
		DOMDocument document = documentProvider.getDocument(uri);
		if (document == null) {

		}
		return executeCommand(document, params, cancelChecker);
	}

	protected abstract Object executeCommand(DOMDocument document, ExecuteCommandParams params,
			CancelChecker cancelChecker) throws Exception;

}
