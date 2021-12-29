package org.eclipse.lemminx.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

class XMLFormatterDocumentNew {

	private final TextDocument textDocument;

	public XMLFormatterDocumentNew(TextDocument textDocument, Range range, SharedSettings sharedSettings,
			Collection<IFormatterParticipant> formatterParticipants) {
		this.textDocument = textDocument;
	}

	public List<? extends TextEdit> format() {
		List<TextEdit> edits = new ArrayList<>();

		DOMDocument document = DOMParser.getInstance().parse(textDocument, null);

		DOMNode currentDOMNode = document;

		XMLFormattingConstraints parentConstraints = getRegionConstraints(currentDOMNode);

		DOMRange formatRange = null;
		formatSiblings(edits, currentDOMNode, parentConstraints, formatRange);

		return edits;
	}

	private void formatSiblings(List<TextEdit> edits, DOMNode currentDOMNode,
			XMLFormattingConstraints parentConstraints, DOMRange formatRange) {
		DOMNode previous = null;
		while (currentDOMNode != null) {
			currentDOMNode = formatRegion(edits, formatRange, parentConstraints, currentDOMNode, previous);
			if (currentDOMNode != null) {
				currentDOMNode = currentDOMNode.getNextSibling();
			}
			previous = currentDOMNode;
		}

	}

	private DOMNode formatRegion(List<TextEdit> edits, DOMRange formatRange, XMLFormattingConstraints parentConstraints,
			DOMNode currentDOMNode, DOMNode previous) {
		// TODO Auto-generated method stub
		return currentDOMNode;
	}

	private XMLFormattingConstraints getRegionConstraints(DOMNode currentDOMNode) {
		XMLFormattingConstraints result = new XMLFormattingConstraints();

		return result;
	}

}
