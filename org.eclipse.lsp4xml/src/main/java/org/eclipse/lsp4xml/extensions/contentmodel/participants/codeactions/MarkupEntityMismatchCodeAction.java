package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * MarkupEntityMismatchCodeAction
 */
public class MarkupEntityMismatchCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		
		
		try {
			int offset = document.offsetAt(diagnostic.getRange().getStart());
			DOMNode node = document.findNodeAt(offset);
			if(!node.isElement()) {
				return;
			}
			
			DOMElement element = (DOMElement) node;
			int startOffset = element.getStartTagOpenOffset();
			Position startPosition = document.positionAt(startOffset);
			Position endPosition = document.positionAt(document.getEnd());
			endPosition.setCharacter(startPosition.getCharacter());
			String elementName = element.getTagName();
			CodeAction action = CodeActionFactory.insert("Close with '</" + elementName + ">'", endPosition, "</" + elementName + ">", document.getTextDocument(), diagnostic);
			codeActions.add(action);
		} catch (BadLocationException e) {
			
		}
		

	}

	
}