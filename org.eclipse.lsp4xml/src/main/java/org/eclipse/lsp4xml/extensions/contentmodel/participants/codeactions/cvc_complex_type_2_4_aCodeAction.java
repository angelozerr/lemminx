package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.LevenshteinDistance;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * cvc_complex_type_2_4_a
 */
public class cvc_complex_type_2_4_aCodeAction implements ICodeActionParticipant {

    private static final float MAX_DISTANCE_DIFF_RATIO = 0.4f;

    @Override
    public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
            XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {

        List<String> names = parseTagNames(diagnostic.getMessage());
        String currentName = names.get(0);

        List<Integer> similarNames = new ArrayList<Integer>();
        List<Integer> otherValidNames = new ArrayList<Integer>();

        for (int i = 1; i < names.size(); i++) {
            String currentSuggestion = names.get(i);

            if (isSimilar(currentSuggestion, currentName)) {
                similarNames.add(i);
            } else {
                otherValidNames.add(i);
            }
        }
        
        int offset;
        try {
            offset = document.offsetAt(range.getStart());
        } catch (BadLocationException e) {
            return;
        }
        DOMNode node = document.findNodeAt(offset);
        
        if(!node.isElement()) {
            return;
        }

        DOMElement element = (DOMElement) node;

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(XMLPositionUtility.selectStartTag(element));
        Range end = XMLPositionUtility.selectEndTag(element);
        if(end != null) {
            ranges.add(end);
        }

        for (Integer integer : similarNames) {
            String elementName = names.get(integer.intValue());
            CodeAction similarCodeAction = CodeActionFactory.replaceAt("Did you mean '" + elementName + "'?",
                    elementName, document.getTextDocument(), diagnostic, ranges);
            codeActions.add(similarCodeAction);
        }

        for (Integer integer : otherValidNames) {
            String elementName = names.get(integer.intValue());
            CodeAction otherCodeAction = CodeActionFactory.replaceAt("Replace with '" + elementName + "'",
                    elementName, document.getTextDocument(), diagnostic, ranges);
            codeActions.add(otherCodeAction);
        }
	}

    /**
     * The structure this parses looks like the following:
     * 
     * ---------------------------
     * 
     * Invalid element name:\n
     *  - nme\n
     * \n
     * One of the following is expected:\n 
     *  - modelVersion\n
     *  - parent\n
     * \n
     * Error indicated by:\n
     *  {http://maven.apache.org/POM/4.0.0}\n
     * with code:
     * 
     * ----------------------------
     */
    private static List<String> parseTagNames(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }

        List<String> names = new ArrayList<String>();
        int i = 0;
        char c = text.charAt(i);

        while (c != '-') {
            i++;
            c = text.charAt(i);
        }

        // Gets current element name
        i = extractElementName(text, i, names);

        c = text.charAt(i);
        // Skip to suggested names
        while (c != '-') {
            i++;
            c = text.charAt(i);
        }

        collectSuggestedNames(text, i, names);
        return names;
    }

    /**
     * Given a string that looks like: "- ELEMENT_NAME\n" it will extract whatever
     * ELEMENT_NAME is and place it in the list of names and return the index of the
     * '\n' character.
     * 
     * @param text
     * @param offset
     * @param names
     * @return
     */
    public static int extractElementName(String text, int offset, List<String> names) {
        int start = offset + 2; // skips over "- " of "- ELEMENT_NAME\n"
        int i = start;
        char c = text.charAt(i);

        while (c != '\n') {
            i++;
            c = text.charAt(i);
        }

        names.add(text.substring(start, i));
        return i;
    }

    /**
     * Works on text that looks like:
     * 
     * " - name1\n" + " - name2\n" + " - name3\n" + "\n"
     * 
     * @param text
     * @param startOffset
     * @param names
     */
    public static void collectSuggestedNames(String text, int startOffset, List<String> names) {
        int i = startOffset;
        i = extractElementName(text, i, names);
        i++; // skips first '\n'
        char c = text.charAt(i);
        if (c == '\n') { // a second '\n' implies we are at end of suggestions
            return;
        }
        i++; // skips space before '-'
        collectSuggestedNames(text, i, names);
    }

    private static boolean isSimilar(String reference, String current) {
        int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
        return levenshteinDistance.apply(reference, current) != -1;
    }
}