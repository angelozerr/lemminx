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
import org.eclipse.lemminx.services.extensions.AbstractDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XSD definition which manages the following definition:
 * 
 * <ul>
 * <li>ref/@name -> define/@name</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class IDREFDefinitionParticipant extends AbstractDefinitionParticipant {

	@Override
	protected boolean match(DOMDocument document) {
		return document.hasGrammar();
	}

	@Override
	protected void doFindDefinition(IDefinitionRequest request, List<LocationLink> locations,
			CancelChecker cancelChecker) {

		// - xref/@ref -> */@id
		DOMNode node = request.getNode();
		if (!node.isAttribute()) {
			return;
		}
		DOMAttr attr = (DOMAttr) node;
		ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
		CMAttributeDeclaration cmAttributeDeclaration = IDREFUtils.getCMAttributeDeclarationAsIDREF(attr,
				contentModelManager);
		if (cmAttributeDeclaration != null) {
			IDREFUtils.searchIDAttributes(attr, cmAttributeDeclaration.getOwnerElementDeclaration().getOwnerDocument(),
					true, true,
					(targetNamespacePrefix, targetAttr) -> {
						LocationLink location = XMLPositionUtility.createLocationLink(attr.getNodeAttrValue(),
								targetAttr.getNodeAttrValue());
						locations.add(location);
					});
		}
	}

}
