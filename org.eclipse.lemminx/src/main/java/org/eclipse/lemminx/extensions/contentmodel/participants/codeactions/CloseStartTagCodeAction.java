/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.LineIndentInfo;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Code action to fix close start tag element.
 *
 */
public class CloseStartTagCodeAction implements ICodeActionParticipant {

	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			SharedSettings sharedSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		try {
			int startOffset = document.offsetAt(diagnosticRange.getStart()) + 1;
			DOMNode node = document.findNodeAt(startOffset);
			if (node == null || !node.isElement()) {
				return;
			}
			DOMElement element = (DOMElement) node;
			boolean startTagClosed = element.isStartTagClosed();
			String text = document.getText();
			if (!startTagClosed) {
				if (!element.isClosed()) {
					// ex : <foo
					// ex : <foo attr="
					// ex : <foo /
					int diagnosticEndOffset = document.offsetAt(diagnosticRange.getEnd());
					boolean hasSlash = isCharAt(text, diagnosticEndOffset - 1, '/');
					if (hasSlash) {
						// ex : <foo /
						CodeAction autoCloseAction = CodeActionFactory.insert("Close with '>'",
								diagnosticRange.getEnd(), ">", document.getTextDocument(), diagnostic);
						codeActions.add(autoCloseAction);
					} else {
						// ex : <foo
						// ex : <foo attr="
						// Close with '/>
						CodeAction autoCloseAction = CodeActionFactory.insert("Close with '/>'",
								diagnosticRange.getEnd(), "/>", document.getTextDocument(), diagnostic);
						codeActions.add(autoCloseAction);
						// // Close with '></foo>
						String tagName = element.getTagName();
						if (tagName != null) {
							String insertText = "></" + tagName + ">";
							CodeAction closeEndTagAction = CodeActionFactory.insert("Close with '" + insertText + "'",
									diagnosticRange.getEnd(), insertText, document.getTextDocument(), diagnostic);
							codeActions.add(closeEndTagAction);
						}
					}
				} else {
					CodeAction autoCloseAction = CodeActionFactory.insert("Close with '>'", diagnosticRange.getEnd(),
							">", document.getTextDocument(), diagnostic);
					codeActions.add(autoCloseAction);
				}
			} else {
				// ex : <foo>
				if (!element.isClosed()) {
					// The element has no an end tag
					// ex : <foo attr="" >
					// // Close with '</foo>'
					String tagName = element.getTagName();
					String label = "</" + tagName + ">";
					final String initialInsertText = label;
					String insertText = initialInsertText;
					Position endPosition = null;

					// - <foo><
					// - <foo> <
					// - <foo></
					// - <foo> </
					if (!element.hasChildNodes()) {
						// ex : <foo><bar></foo>
						int endOffset = element.getStartTagCloseOffset() + 1;
						endPosition = document.positionAt(endOffset);

						CodeAction closeEndTagAction = CodeActionFactory.insert("Close with '" + label + "'",
								endPosition, insertText, document.getTextDocument(), diagnostic);
						codeActions.add(closeEndTagAction);

					} else {
						// the element have some children(Text node, Element node, etc)

						boolean stop = false;
						// Search orphan elements in the children
						List<DOMNode> children = element.getChildren();
						for (DOMNode child : children) {
							if (child.isElement()) {
								DOMElement childElement = (DOMElement) child;
								if (childElement.getTagName() == null) {
									Position startPosition = document.positionAt(childElement.getStart());
									endPosition = document.positionAt(childElement.getEnd());
									boolean hasStartTag = childElement.hasStartTag();
									CodeAction replaceAction = CodeActionFactory.replace(
											"Replace '" + (hasStartTag ? "<" : "</") + "' with '" + label + "'",
											new Range(startPosition, endPosition), initialInsertText,
											document.getTextDocument(), diagnostic);
									codeActions.add(replaceAction);
									stop = true;
								} else if (childElement.isOrphanEndTag()) {
									CodeAction replaceTagAction = createReplaceTagNameCodeAction(childElement, element,
											diagnostic);
									if (replaceTagAction != null) {
										codeActions.add(replaceTagAction);
									}
								}
							}
						}

						if (!stop) {
							int endOffset = element.getLastChild().getEnd() - 1;
							if (endOffset < text.length()) {
								// remove whitespaces
								char ch = text.charAt(endOffset);
								while (Character.isWhitespace(ch)) {
									endOffset--;
									ch = text.charAt(endOffset);
								}
							}
							endOffset++;
							endPosition = document.positionAt(endOffset);

							if (hasElements(element)) {
								// The element have element node as children
								// the </foo> must be inserted with a new line and indent
								LineIndentInfo indentInfo = document
										.getLineIndentInfo(diagnosticRange.getStart().getLine());
								insertText = indentInfo.getLineDelimiter() + indentInfo.getWhitespacesIndent()
										+ insertText;
							}

							CodeAction closeEndTagAction = CodeActionFactory.insert("Close with '" + label + "'",
									endPosition, insertText, document.getTextDocument(), diagnostic);
							codeActions.add(closeEndTagAction);
						}
					}
				} else {
					// The element has an end tag

					// Search orphan elements in the children
					List<DOMNode> children = element.getChildren();
					for (DOMNode child : children) {
						if (child.isElement()) {
							DOMElement childElement = (DOMElement) child;
							if (childElement.getTagName() == null) {
								Position startPosition = document.positionAt(childElement.getStart());
								Position endPosition = document.positionAt(childElement.getEnd());
								boolean hasStartTag = childElement.hasStartTag();
								CodeAction replaceAction = CodeActionFactory.remove(
										"Remove '" + (hasStartTag ? "<" : "</") + "'",
										new Range(startPosition, endPosition), document.getTextDocument(), diagnostic);
								codeActions.add(replaceAction);
							} else if (childElement.isOrphanEndTag()) {
								/*
								 * CodeAction replaceTagAction = createReplaceTagNameCodeAction(childElement,
								 * element, diagnostic); if (replaceTagAction != null) {
								 * codeActions.add(replaceTagAction); }
								 */
							}
						}
					}
				}
			}
		} catch (BadLocationException e) {
			// do nothing
		}
	}

	/**
	 * Create a code action which replace the tag name of the given element with the
	 * tag name of the parent element.
	 * 
	 * @param element
	 * @param parent
	 * @param diagnostic
	 * @param codeActions
	 * @return
	 */
	private static CodeAction createReplaceTagNameCodeAction(DOMElement element, DOMElement parent,
			Diagnostic diagnostic) {
		// <a><b></c>
		// Replace with 'b' closing tag
		DOMDocument document = element.getOwnerDocument();
		String replaceTagName = parent.getTagName();
		Range replaceRange = XMLPositionUtility.selectEndTagName(element);
		String tagName = element.getTagName();
		String replaceText = replaceTagName;
		if (!element.isEndTagClosed()) {
			replaceText = replaceText + ">";
		}
		return CodeActionFactory.replace("Replace '" + tagName + "' with '" + replaceTagName + "' closing tag",
				replaceRange, replaceText, document.getTextDocument(), diagnostic);
	}

	/**
	 * Returns true if the given element has elements as children and false
	 * otherwise.
	 * 
	 * @param element the DOM element.
	 * 
	 * @return true if the given element has elements as children and false
	 *         otherwise.
	 */
	private static boolean hasElements(DOMElement element) {
		for (DOMNode node : element.getChildren()) {
			if (node.isElement()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCharAt(String text, int offset, char ch) {
		if (text.length() <= offset) {
			return false;
		}
		return text.charAt(offset) == ch;
	}

}
