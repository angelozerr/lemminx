package org.eclipse.lemminx.extensions.idref;

import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IHighlightingParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lsp4j.InitializeParams;

public class IDREFPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;
	private final IDefinitionParticipant definitionParticipant;
	private final IHighlightingParticipant highlightingParticipant;
	private ContentModelManager contentModelManager;

	public IDREFPlugin() {
		completionParticipant = new IDREFCompletionParticipant();
		definitionParticipant = new IDREFDefinitionParticipant();
		highlightingParticipant = new IDREFHighlightingParticipant(this);
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		contentModelManager = registry.getComponent(ContentModelManager.class);
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDefinitionParticipant(definitionParticipant);
		registry.registerHighlightingParticipant(highlightingParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterDefinitionParticipant(definitionParticipant);
		registry.unregisterHighlightingParticipant(highlightingParticipant);
	}

	public ContentModelManager getContentModelManager() {
		return contentModelManager;
	}

}
