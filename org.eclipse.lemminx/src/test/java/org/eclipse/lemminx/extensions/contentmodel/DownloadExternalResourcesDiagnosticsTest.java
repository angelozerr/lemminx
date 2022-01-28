package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.pd;

import java.io.File;

import org.apache.xerces.impl.XMLEntityManager;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

public class DownloadExternalResourcesDiagnosticsTest {

	@Test
	public void localDTD() throws Exception {

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		// Use cache on file system
		contentModelManager.setUseCache(true);

		String fileURI = "test.xml";

		// Valid DTD path
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base.dtd\" >\r\n" + //
				"<root-element>\r\n" + //
				"  <bar />\r\n" + // <-- error
				"</root-element>";

		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, ls, pd(fileURI, //
				d(3, 3, 3, 6, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED, //
						"Element type \"bar\" must be declared.", "xml", DiagnosticSeverity.Error), //
				d(2, 1, 2, 13, DTDErrorCode.MSG_CONTENT_INVALID, //
						"The content of element type \"root-element\" must match \"null\".", "xml",
						DiagnosticSeverity.Error)));

		// Invalid DTD path
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"BAD/src/test/resources/dtd/entities/base.dtd\" >\r\n" + //
				"<root-element>\r\n" + //
				"  <bar />\r\n" + // <-- error
				"</root-element>";

		File f = new File("BAD/src/test/resources/dtd/entities/base.dtd");
		String dtdPath = XMLEntityManager.expandSystemId(f.getCanonicalPath(), null, false);

		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, ls, pd(fileURI, //
				d(1, 30, 1, 76, DTDErrorCode.dtd_not_found, //
						"Cannot find DTD '" + dtdPath + "'.", "xml", DiagnosticSeverity.Error), //
				d(2, 1, 2, 13, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED, //
						"Element type \"root-element\" must be declared.", "xml", DiagnosticSeverity.Error), //
				d(3, 3, 3, 6, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED, //
						"Element type \"bar\" must be declared.", "xml", DiagnosticSeverity.Error)));
	}

	@Test
	public void ù() throws Exception {

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		// Use cache on file system
		contentModelManager.setUseCache(true);

		String fileURI = "test.xml";

		// Invalid DTD path
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"http://base.dtd\" >\r\n" + //
				"<root-element>\r\n" + //
				"  <bar />\r\n" + // <-- error
				"</root-element>";

		File f = new File("BAD/src/test/resources/dtd/entities/base.dtd");
		String dtdPath = XMLEntityManager.expandSystemId(f.getCanonicalPath(), null, false);

		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, ls, pd(fileURI, //
				d(1, 30, 1, 76, DTDErrorCode.dtd_not_found, //
						"Cannot find DTD '" + dtdPath + "'.", "xml", DiagnosticSeverity.Error), //
				d(2, 1, 2, 13, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED, //
						"Element type \"root-element\" must be declared.", "xml", DiagnosticSeverity.Error), //
				d(3, 3, 3, 6, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED, //
						"Element type \"bar\" must be declared.", "xml", DiagnosticSeverity.Error)));
	}
}
