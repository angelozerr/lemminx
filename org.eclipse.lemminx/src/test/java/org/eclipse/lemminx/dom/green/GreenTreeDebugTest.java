package org.eclipse.lemminx.dom.green;

import org.eclipse.lemminx.dom.*;
import org.junit.jupiter.api.Test;

public class GreenTreeDebugTest {
    @Test
    public void debugDoctypeNoInternalSubset() {
        // Same input as testDoctypeNoInternalSubset
        String content = "<!DOCTYPE    note\r\n\r\n>\r\n<note>\r\n  <to>Fred</to>\r\n</note>";
        DOMDocument doc = DOMParser.getInstance().parse(content, "test.xml", null);
        System.out.println("Children count: " + doc.getChildren().size());
        for (DOMNode child : doc.getChildren()) {
            System.out.println("  Child: " + child.getClass().getSimpleName() + " start=" + child.getStart() + " end=" + child.getEnd());
            if (child instanceof DOMDocumentType) {
                DOMDocumentType dt = (DOMDocumentType) child;
                System.out.println("    name=" + dt.getName() + " closed=" + dt.isClosed());
                System.out.println("    parameters count: " + dt.getParameters().size());
                for (int i = 0; i < dt.getParameters().size(); i++) {
                    DTDDeclParameter p = dt.getParameters().get(i);
                    System.out.println("      param[" + i + "] start=" + p.getStart() + " end=" + p.getEnd() + " val='" + content.substring(p.getStart(), p.getEnd()) + "'");
                }
                DTDDeclParameter nameParam = dt.getNameParameter();
                if (nameParam != null) {
                    System.out.println("    nameParam start=" + nameParam.getStart() + " end=" + nameParam.getEnd());
                }
                System.out.println("    internalSubset=" + dt.getInternalSubsetNode());
            }
        }
    }
    
    @Test
    public void debugDoctypeWithInternalSubset() {
        String content = "<!DOCTYPE foo [\n<!ELEMENT foo EMPTY>\n]>";
        DOMDocument doc = DOMParser.getInstance().parse(content, "test.xml", null);
        System.out.println("Children count: " + doc.getChildren().size());
        for (DOMNode child : doc.getChildren()) {
            System.out.println("  Child: " + child.getClass().getSimpleName() + " start=" + child.getStart() + " end=" + child.getEnd());
            if (child instanceof DOMDocumentType) {
                DOMDocumentType dt = (DOMDocumentType) child;
                System.out.println("    name=" + dt.getName() + " closed=" + dt.isClosed());
                System.out.println("    parameters count: " + dt.getParameters().size());
                for (int i = 0; i < dt.getParameters().size(); i++) {
                    DTDDeclParameter p = dt.getParameters().get(i);
                    System.out.println("      param[" + i + "] start=" + p.getStart() + " end=" + p.getEnd() + " val='" + content.substring(p.getStart(), p.getEnd()) + "'");
                }
                DTDDeclParameter nameParam = dt.getNameParameter();
                if (nameParam != null) {
                    System.out.println("    nameParam start=" + nameParam.getStart() + " end=" + nameParam.getEnd());
                }
                DTDDeclParameter internalSubset = dt.getInternalSubsetNode();
                if (internalSubset != null) {
                    System.out.println("    internalSubset start=" + internalSubset.getStart() + " end=" + internalSubset.getEnd());
                }
                System.out.println("    children: " + dt.getChildren().size());
                for (DOMNode dtChild : dt.getChildren()) {
                    System.out.println("      dtChild: " + dtChild.getClass().getSimpleName() + " start=" + dtChild.getStart() + " end=" + dtChild.getEnd());
                    if (dtChild instanceof DTDDeclNode) {
                        DTDDeclNode decl = (DTDDeclNode) dtChild;
                        System.out.println("        name=" + decl.getName() + " params=" + decl.getParameters().size());
                        for (int i = 0; i < decl.getParameters().size(); i++) {
                            DTDDeclParameter p = decl.getParameters().get(i);
                            System.out.println("          param[" + i + "] start=" + p.getStart() + " end=" + p.getEnd() + " val='" + content.substring(p.getStart(), p.getEnd()) + "'");
                        }
                    }
                }
            }
        }
    }
}
