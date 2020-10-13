package org.eclipse.lemminx.extensions.contentmodel.command;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.IXMLValidationService;
import org.eclipse.lemminx.services.extensions.commands.AbstractDOMDocumentCommandHandler;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class XMLValidationFileCommand extends AbstractDOMDocumentCommandHandler {

	public static final String COMMAND_ID = "xml.validation.current.file";

	private final ContentModelManager contentModelManager;

	private final IXMLValidationService validationService;

	public XMLValidationFileCommand(ContentModelManager contentModelManager, IXMLDocumentProvider documentProvider,
			IXMLValidationService validationService) {
		super(documentProvider);
		this.contentModelManager = contentModelManager;
		this.validationService = validationService;
	}

	@Override
	protected Object executeCommand(DOMDocument document, ExecuteCommandParams params, CancelChecker cancelChecker)
			throws Exception {
		contentModelManager.evictCacheFor(document);
		validationService.validate(document);
		return null;
	}

}
