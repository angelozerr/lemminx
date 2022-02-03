package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.msg.XMLModelMessageFormatter;

class LSPXMLEntityManager extends XMLEntityManager {

	private final XMLErrorReporter errorReporter;

	private final LSPXMLGrammarPool grammarPool;
	private boolean hasProblemsWithReferencedDTD;

	public LSPXMLEntityManager(XMLErrorReporter errorReporter, LSPXMLGrammarPool grammarPool) {
		this.errorReporter = errorReporter;
		this.grammarPool = grammarPool;
		this.hasProblemsWithReferencedDTD = false;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws IOException, XNIException {
		if(resourceIdentifier instanceof XSDDescription) {
			return super.resolveEntity(resourceIdentifier);
		}
		try {
			return super.resolveEntity(resourceIdentifier);
		} catch (Exception e) {
			reportError(resourceIdentifier.getLiteralSystemId(), e);
			XMLInputSource in = new XMLInputSource(resourceIdentifier);
			in.setCharacterStream(new StringReader(""));
			return in;
		}
	}

	@Override
	public String setupCurrentEntity(String name, XMLInputSource xmlInputSource, boolean literal, boolean isExternal)
			throws IOException, XNIException {		
		try {
			return super.setupCurrentEntity(name, xmlInputSource, literal, isExternal);
		} catch (Exception e) {
			reportError(xmlInputSource.getSystemId(), e);
			XMLInputSource in = new XMLInputSource(xmlInputSource.getPublicId(), xmlInputSource.getSystemId(),
					xmlInputSource.getBaseSystemId(), new StringReader(""), null);
			return super.setupCurrentEntity(name, in, literal, isExternal);
		}
	}

	private void reportError(String location, Exception e) {
		hasProblemsWithReferencedDTD = true;
		errorReporter.reportError(new XMLLocator() {

			@Override
			public String getXMLVersion() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPublicId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLiteralSystemId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getLineNumber() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getExpandedSystemId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getColumnNumber() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getCharacterOffset() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getBaseSystemId() {
				// TODO Auto-generated method stub
				return null;
			}
		}, XMLModelMessageFormatter.XML_MODEL_DOMAIN, DTDErrorCode.dtd_not_found.getCode(),
				new Object[] { null, location }, XMLErrorReporter.SEVERITY_ERROR, e);
	}

	public void dispose() {
		//if (hasProblemsWithReferencedDTD) {
			grammarPool.clear();
		//}
		// problems.forEach(uri -> grammarPool.removeGrammar(uri));
	}
}