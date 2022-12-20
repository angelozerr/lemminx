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

import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.idref.utils.IDREFUtils;
import org.eclipse.lemminx.services.extensions.IHighlightingParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * RNG highlight participant
 * 
 * @author Angelo ZERR
 *
 */
public class IDREFHighlightingParticipant implements IHighlightingParticipant {

	private final IDREFPlugin plugin;

	public IDREFHighlightingParticipant(IDREFPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void findDocumentHighlights(DOMNode node, Position position, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		// XSD highlight applicable only for rng file
		DOMDocument document = node.getOwnerDocument();
		// Highlight works only when attribute is selected (origin or target attribute)
		DOMAttr attr = node.findAttrAt(offset);
		if (attr == null || attr.getNodeAttrValue() == null) {
			return;
		}

		CMAttributeDeclaration cmAttributeDeclaration = IDREFUtils.getCMAttributeDeclarationAsIDREF(attr,
				plugin.getContentModelManager());
		if (cmAttributeDeclaration != null) {
			// It's an origin attribute, highlight the origin and target attribute
			DOMAttr originAttr = attr;
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(originAttr.getNodeAttrValue().getStart(),
							originAttr.getNodeAttrValue().getEnd(), document), DocumentHighlightKind.Read));
			// Search target attributes only in the XML Schema and not in xs:include since
			// LSP highlighting works only for a given file
			boolean searchInExternalSchema = false;
			IDREFUtils.searchIDAttributes(attr, cmAttributeDeclaration.getOwnerElementDeclaration().getOwnerDocument(),
					true, searchInExternalSchema,
					(targetNamespacePrefix, targetAttr) -> {
						highlights.add(new DocumentHighlight(
								XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
										targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
								DocumentHighlightKind.Write));
					});
		}
//
//		// Try to get the binding from the origin attribute
//		BindingType bindingType = RelaxNGUtils.getBindingType(attr);
//		if (bindingType != BindingType.NONE) {
//			// It's an origin attribute, highlight the origin and target attribute
//			DOMAttr originAttr = attr;
//			highlights
//					.add(new DocumentHighlight(XMLPositionUtility.createRange(originAttr.getNodeAttrValue().getStart(),
//							originAttr.getNodeAttrValue().getEnd(), document), DocumentHighlightKind.Read));
//			// Search target attributes only in the XML Schema and not in xs:include since
//			// LSP highlighting works only for a given file
//			boolean searchInExternalSchema = false;
//			RelaxNGUtils.searchRNGTargetAttributes(originAttr, bindingType, true, searchInExternalSchema,
//					(targetNamespacePrefix, targetAttr) -> {
//						highlights.add(new DocumentHighlight(
//								XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
//										targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
//								DocumentHighlightKind.Write));
//					});
//
//		} else if (RelaxNGUtils.isRNGTargetElement(attr.getOwnerElement())) {
//			// It's an target attribute, highlight all origin attributes linked to this
//			// target attribute
//			DOMAttr targetAttr = attr;
//			highlights.add(new DocumentHighlight(
//					XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
//							targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
//					DocumentHighlightKind.Write));
//			RelaxNGUtils.searchRNGOriginAttributes(targetAttr,
//					(origin, target) -> highlights.add(new DocumentHighlight(
//							XMLPositionUtility.createRange(origin.getNodeAttrValue().getStart(),
//									origin.getNodeAttrValue().getEnd(), origin.getOwnerDocument()),
//							DocumentHighlightKind.Read)),
//					cancelChecker);
//		}
	}

}
