/**
 *  Copyright (c) 2018-2020 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.dtd.participants;

import static org.eclipse.lemminx.utils.XMLPositionUtility.createDocumentLink;

import java.util.Collection;
import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.dom.DTDEntityDecl;
import org.eclipse.lemminx.dom.NoNamespaceSchemaLocation;
import org.eclipse.lemminx.dom.SchemaLocation;
import org.eclipse.lemminx.dom.SchemaLocationHint;
import org.eclipse.lemminx.dom.XMLModel;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4j.DocumentLink;
import org.w3c.dom.NamedNodeMap;

/**
 * Document link for :
 *
 * <ul>
 * <li>XML Schema xsi:noNamespaceSchemaLocation</li>
 * <li>DTD SYSTEM (ex : <!DOCTYPE root-element SYSTEM "./extended.dtd" )</li>
 * <li>XML Schema xsi:schemaLocation</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class DTDDocumentLinkParticipant implements IDocumentLinkParticipant {

	private final URIResolverExtensionManager resolverManager;

	public DTDDocumentLinkParticipant(URIResolverExtensionManager resolverManager) {
		this.resolverManager = resolverManager;
	}

	@Override
	public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
		// Document link for DTD
		DOMDocumentType docType = document.getDoctype();
		if (docType != null) {
			String location = resolverManager.resolve(document.getDocumentURI(), docType.getPublicIdWithoutQuotes(),
					docType.getSystemIdWithoutQuotes());
			if (location != null) {
				try {
					DOMRange systemIdRange = docType.getSystemIdNode();
					if (systemIdRange != null) {
						links.add(createDocumentLink(systemIdRange, location, true));
					}
				} catch (BadLocationException e) {
					// Do nothing
				}
			}

			NamedNodeMap entities = docType.getEntities();
			for (int i = 0; i < entities.getLength(); i++) {
				DTDEntityDecl entity = (DTDEntityDecl) entities.item(i);
				location = resolverManager.resolve(document.getDocumentURI(), entity.getPublicId(),
						entity.getSystemId());
				if (location != null) {
					try {
						DOMRange systemIdRange = entity.getSystemIdNode();
						if (systemIdRange != null) {
							links.add(createDocumentLink(systemIdRange, location, true));
						}
					} catch (BadLocationException e) {
						// Do nothing
					}
				}
			}
		}
	}

}
