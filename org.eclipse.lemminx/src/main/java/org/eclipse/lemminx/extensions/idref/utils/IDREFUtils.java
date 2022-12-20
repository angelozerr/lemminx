package org.eclipse.lemminx.extensions.idref.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.relaxng.utils.RelaxNGUtils.BindingType;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.URIUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

public class IDREFUtils {

	private static final String INCLUDE_TAG = "include";
	private static final String HREF_ATTR = "href";

	public static void searchIDAttributes(DOMAttr originAttr, CMDocument cmDocument,
			boolean matchAttr,
			boolean searchInExternalSchema, BiConsumer<String, DOMAttr> collector) {
		DOMDocument document = originAttr.getOwnerDocument();
		DOMElement documentElement = document != null ? document.getDocumentElement() : null;
		if (documentElement == null) {
			return;
		}
		String originAttrValue = originAttr.getValue();
		if (matchAttr && StringUtils.isEmpty(originAttrValue)) {
			return;
		}
		String targetNamespacePrefix = null;
		int index = originAttrValue.indexOf(':');
		if (index != -1) {
			// ex : jakartaee:applicationType
			targetNamespacePrefix = originAttrValue.substring(0, index);
		}

		String originName = null;
		if (matchAttr) {
			originName = getOriginName(originAttrValue, targetNamespacePrefix);
		}
		// Loop for element define.
		searchIDAttributes(originAttr, cmDocument, matchAttr, collector, documentElement, targetNamespacePrefix,
				originName, new HashSet<>(), searchInExternalSchema);
	}

	private static void searchIDAttributes(DOMAttr originAttr, CMDocument cmDocument, boolean matchAttr,
			BiConsumer<String, DOMAttr> collector, DOMElement documentElement, String targetNamespacePrefix,
			String originName, Set<String> visitedURIs, boolean searchInExternalSchema) {
		if (visitedURIs != null) {
			DOMDocument document = documentElement.getOwnerDocument();
			String documentURI = document.getDocumentURI();
			if (visitedURIs.contains(documentURI)) {
				return;
			}
			visitedURIs.add(documentURI);
		}
		Set<String> externalURIS = null;
		Node parent = documentElement;
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement targetElement = (DOMElement) node;
				CMElementDeclaration cmElement = cmDocument.findCMElement(targetElement,
						targetElement.getNamespaceURI());
				if (cmElement != null) {
					for (CMAttributeDeclaration attributeDecl : cmElement.getAttributes()) {
						if (attributeDecl.isID()) {
							DOMAttr targetAttr = (DOMAttr) targetElement.getAttributeNode(attributeDecl.getLocalName());
							if (targetAttr != null
									&& (!matchAttr || Objects.equal(originName, targetAttr.getValue()))) {
								collector.accept(targetNamespacePrefix, targetAttr);
							}
						}
					}
				}

				if (isInclude(targetElement)) {
					// collect include RNG Schema location
					String schemaLocation = targetElement.getAttribute(HREF_ATTR);
					if (schemaLocation != null) {
						if (externalURIS == null) {
							externalURIS = new HashSet<>();
						}
						externalURIS.add(schemaLocation);
					}
				} else {
					searchIDAttributes(originAttr, cmDocument, matchAttr, collector, targetElement,
							targetNamespacePrefix, originName, null, false);
				}
			}
		}
		if (searchInExternalSchema && externalURIS != null) {
			// Search in include location
			DOMDocument document = documentElement.getOwnerDocument();
			String documentURI = document.getDocumentURI();
			URIResolverExtensionManager resolverExtensionManager = document.getResolverExtensionManager();
			for (String externalURI : externalURIS) {
				String resourceURI = resolverExtensionManager.resolve(documentURI, null, externalURI);
				if (URIUtils.isFileResource(resourceURI)) {
					DOMDocument externalDocument = DOMUtils.loadDocument(resourceURI,
							document.getResolverExtensionManager());
					if (externalDocument != null) {
						searchIDAttributes(originAttr, cmDocument, matchAttr, collector,
								externalDocument.getDocumentElement(), targetNamespacePrefix, originName, visitedURIs,
								searchInExternalSchema);
					}
				}
			}
		}
	}

	private static String getOriginName(String originAttrValue, String targetNamespacePrefix) {
		int index = originAttrValue.indexOf(":");
		if (index != -1) {
			String prefix = originAttrValue.substring(0, index);
			if (!Objects.equal(prefix, targetNamespacePrefix)) {
				return null;
			}
			return originAttrValue.substring(index + 1, originAttrValue.length());
		}
		return originAttrValue;
	}

	public static CMAttributeDeclaration getCMAttributeDeclarationAsIDREF(DOMAttr attr,
			ContentModelManager contentModelManager) {
		CMAttributeDeclaration cmAttribute = findCMAttributeDeclaration(attr, contentModelManager);
		return cmAttribute != null && cmAttribute.isIDREF() ? cmAttribute : null;
	}

	private static CMAttributeDeclaration findCMAttributeDeclaration(DOMAttr attr,
			ContentModelManager contentModelManager) {
		DOMElement parentElement = attr.getOwnerElement();
		Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(parentElement);
		for (CMDocument cmDocument : cmDocuments) {
			CMAttributeDeclaration cmAttribute = findCMAttributeDeclaration(attr, cmDocument);
			if (cmAttribute != null) {
				return cmAttribute;
			}
		}
		return null;
	}

	private static CMAttributeDeclaration findCMAttributeDeclaration(DOMAttr attr, CMDocument cmDocument) {
		DOMElement parentElement2 = attr.getOwnerElement();
		CMElementDeclaration cmElement = cmDocument.findCMElement(parentElement2,
				parentElement2.getNamespaceURI());
		if (cmElement != null) {
			return cmElement.findCMAttribute(attr);
		}
		return null;
	}

	public static boolean isInclude(Element element) {
		return element != null && INCLUDE_TAG.equals(element.getLocalName());
	}
}
