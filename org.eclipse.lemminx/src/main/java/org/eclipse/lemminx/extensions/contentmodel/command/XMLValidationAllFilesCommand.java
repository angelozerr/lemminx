package org.eclipse.lemminx.extensions.contentmodel.command;

import java.util.Collection;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.IXMLCommandService.IDelegateCommandHandler;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.IXMLValidationService;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class XMLValidationAllFilesCommand implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "xml.validation.all.files";

	private final ContentModelManager contentModelManager;

	private final IXMLDocumentProvider documentProvider;

	private final IXMLValidationService validationService;

	public XMLValidationAllFilesCommand(ContentModelManager contentModelManager, IXMLDocumentProvider documentProvider,
			IXMLValidationService validationService) {
		this.contentModelManager = contentModelManager;
		this.documentProvider = documentProvider;
		this.validationService = validationService;
	}

	@Override
	public Object executeCommand(ExecuteCommandParams params, CancelChecker cancelChecker) throws Exception {
		contentModelManager.evictCache();
		Collection<DOMDocument> all = documentProvider.getAllDocuments();
		for (DOMDocument document : all) {
			validationService.validate(document);
		}
		return null;
	}

}
