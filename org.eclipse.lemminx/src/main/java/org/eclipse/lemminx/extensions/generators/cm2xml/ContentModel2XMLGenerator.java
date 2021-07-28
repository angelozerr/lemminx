package org.eclipse.lemminx.extensions.generators.cm2xml;

import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.generators.IFileContentGenerator;
import org.eclipse.lemminx.services.IXMLFullFormatter;
import org.eclipse.lemminx.settings.SharedSettings;

public class ContentModel2XMLGenerator implements IFileContentGenerator<CMDocument, ContentModelGeneratorSettings> {

	@Override
	public String generate(CMDocument document, SharedSettings sharedSettings,
			ContentModelGeneratorSettings generatorSettings, IXMLFullFormatter formatter) {
		// TODO Auto-generated method stub
		return null;
	}

}
