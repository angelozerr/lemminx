/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.idref;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.idref.utils.IDREFUtils;
import org.eclipse.lemminx.extensions.xsd.DataType;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class IDREFCompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
			CancelChecker cancelChecker) throws Exception {
		DOMNode node = request.getNode();

		Range fullRange = request.getReplaceRange();
		DOMAttr originAttr = node.findAttrAt(request.getOffset());

		ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
		CMAttributeDeclaration cmAttributeDeclaration = IDREFUtils.getCMAttributeDeclarationAsIDREF(originAttr,
				contentModelManager);
		if (cmAttributeDeclaration != null) {
			IDREFUtils.searchIDAttributes(originAttr,
					cmAttributeDeclaration.getOwnerElementDeclaration().getOwnerDocument(),
					false, true,
					(targetNamespacePrefix, targetAttr) -> {
						CompletionItem item = new CompletionItem();
						item.setDocumentation(
								new MarkupContent(MarkupKind.MARKDOWN, DataType.getDocumentation(targetAttr)));
						String value = createComplexTypeValue(targetAttr, targetNamespacePrefix);
						String insertText = request.getInsertAttrValue(value);
						item.setLabel(value);
						item.setKind(CompletionItemKind.Value);
						item.setFilterText(insertText);
						item.setTextEdit(Either.forLeft(new TextEdit(fullRange, insertText)));
						response.addCompletionItem(item);
					});
		}
	}

	private static String createComplexTypeValue(DOMAttr targetAttr, String targetNamespacePrefix) {
		StringBuilder value = new StringBuilder();
		if (targetNamespacePrefix != null) {
			value.append(targetNamespacePrefix);
			value.append(":");
		}
		value.append(targetAttr.getValue());
		return value.toString();
	}
}
