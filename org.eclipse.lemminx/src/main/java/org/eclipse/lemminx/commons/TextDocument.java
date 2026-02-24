/**
 *  Copyright (c) 2018, 2023 Angelo ZERR.
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
package org.eclipse.lemminx.commons;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lemminx.commons.text.CompositeCharSequence;
import org.eclipse.lemminx.commons.text2.ImmutableCharSequence;
import org.eclipse.lemminx.commons.text.ImmutableCharSequenceImpl;
import org.eclipse.lemminx.commons.text2.CharArrayUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Text document extends LSP4j {@link TextDocumentItem} to provide methods to
 * retrieve position.
 *
 */
public class TextDocument {

	private static final Logger LOGGER = Logger.getLogger(TextDocument.class.getName());

	// Consolidate CompositeCharSequence after this many updates to prevent deep
	// nesting
	private static final int CONSOLIDATION_THRESHOLD = 100;

	/**
	 * The text document's uri.
	 */
	@NonNull
	private String uri;

	/**
	 * The text document's language identifier
	 */
	@NonNull
	private String languageId;

	/**
	 * The version number of this document (it will strictly increase after each
	 * change, including undo/redo).
	 */
	private int version;

	/**
	 * The content of the opened text document.
	 */
	// CharSequence-based text storage for memory-efficient incremental updates
	//@NonNull
	//private ImmutableCharSequence text;

	private final Object lock = new Object();

	private static String DEFAULT_DELIMTER = System.lineSeparator();

	private ILineTracker lineTracker;

	private boolean incremental;

	// Counter for tracking when to consolidate CompositeCharSequence
	private int updatesSinceConsolidation = 0;

	private org.eclipse.lemminx.commons.text2.ImmutableCharSequence myText;

	public TextDocument(TextDocumentItem document) {
		this(document.getText(), document.getUri());
		this.setVersion(document.getVersion());
		this.setLanguageId(document.getLanguageId());
	}

	public TextDocument(String text, String uri) {
		this.setUri(uri);
		setText(text); 
	}

	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
		// reset line tracker
		lineTracker = null;
		getLineTracker();
	}

	public boolean isIncremental() {
		return incremental;
	}

	public Position positionAt(int position) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		return lineTracker.getPositionAt(position);
	}

	public int offsetAt(Position position) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		return lineTracker.getOffsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		Line line = lineTracker.getLineInformation(lineNumber);
		CharSequence text = getTextSequence();
		return text.subSequence(line.offset, line.offset + line.length).toString();
	}

	public int lineOffsetAt(int position) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		Line line = lineTracker.getLineInformationOfOffset(position);
		return line.offset;
	}

	/**
	 * Returns the line number the character at the given offset belongs to.
	 *
	 * @param position the offset whose line number to be determined
	 * @return the number of the line the offset is on
	 * @exception BadLocationException if the offset is invalid in this tracker
	 */
	public int lineAt(int position) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		return lineTracker.getLineNumberOfOffset(position);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		ILineTracker lineTracker = getLineTracker();
		String lineDelimiter = lineTracker.getLineDelimiter(lineNumber);
		if (lineDelimiter == null) {
			if (lineTracker.getNumberOfLines() > 0) {
				lineDelimiter = lineTracker.getLineInformation(0).delimiter;
			}
		}
		if (lineDelimiter == null) {
			lineDelimiter = DEFAULT_DELIMTER;
		}
		return lineDelimiter;
	}

	public Range getWordRangeAt(int textOffset, Pattern wordDefinition) {
		try {
			Position pos = positionAt(textOffset);
			ILineTracker lineTracker = getLineTracker();
			Line line = lineTracker.getLineInformation(pos.getLine());
			CharSequence text = getTextSequence();
			String lineText = text.subSequence(line.offset, textOffset).toString();
			int position = lineText.length();
			Matcher m = wordDefinition.matcher(lineText);
			int currentPosition = 0;
			while (currentPosition != position) {
				if (m.find()) {
					currentPosition = m.end();
					if (currentPosition == position) {
						return new Range(new Position(pos.getLine(), m.start()), pos);
					}
				} else {
					currentPosition++;
				}
				m.region(currentPosition, position);
			}
			return new Range(pos, pos);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private ILineTracker getLineTracker() {
		if (lineTracker == null) {
			lineTracker = createLineTracker();
		}
		return lineTracker;
	}

	private synchronized ILineTracker createLineTracker() {
		if (lineTracker != null) {
			return lineTracker;
		}
		ILineTracker lineTracker = isIncremental() ? new TreeLineTracker(new ListLineTracker()) : new ListLineTracker();
		lineTracker.set(getTextSequence());
		return lineTracker;
	}

	/**
	 * Update text of the document by using the changes and according the
	 * incremental support.
	 * 
	 * @param changes the text document changes.
	 */
	public void update(List<TextDocumentContentChangeEvent> changes) {
		if (changes.size() < 1) {
			// no changes, ignore it.
			return;
		}
		if (isIncremental()) {
			try {
				long start = System.currentTimeMillis();
				synchronized (lock) {
					// Get current text as CharSequence (no copy)
					CharSequence currentText = getTextSequence();

					// Loop for each changes and update the buffer
					for (int i = 0; i < changes.size(); i++) {

						TextDocumentContentChangeEvent changeEvent = changes.get(i);
						Range range = changeEvent.getRange();

						if (range != null) {
							Integer rangeLength = changeEvent.getRangeLength();
							int startOffset = offsetAt(range.getStart());
							int length;

							if (rangeLength != null) {
								// Use rangeLength if provided (preferred)
								length = rangeLength.intValue();
							} else {
								// Calculate length from range.end
								int endOffset = offsetAt(range.getEnd());
								length = endOffset - startOffset;
							}

							String text = changeEvent.getText();

							// Use CompositeCharSequence for zero-copy update
							// This avoids copying the entire document text
							//ImmutableCharSequence newText = CompositeCharSequence.replaceRange(currentText, startOffset,
							//		startOffset + length, ImmutableCharSequenceImpl.fromString(text));

							lineTracker.replace(startOffset, length, text);
							replaceString(startOffset, startOffset + length, 0, text, -1, false);
							//setText(newText);

							// IMPORTANT: Update currentText for next iteration
							//currentText = newText;

							// Periodically consolidate to prevent deep nesting
						} else {
							// Full replacement
							setText(changeEvent.getText());
							lineTracker.set(changeEvent.getText());
							currentText = getTextSequence();
							updatesSinceConsolidation = 0;
						}
					}
				}
				LOGGER.fine("Text document content updated in " + (System.currentTimeMillis() - start) + "ms");
			} catch (BadLocationException e) {
				// Should never occur.
			}
		} else {
			// like vscode does, get the last changes
			// see
			// https://github.com/Microsoft/vscode-languageserver-node/blob/master/server/src/main.ts
			TextDocumentContentChangeEvent last = changes.size() > 0 ? changes.get(changes.size() - 1) : null;
			if (last != null) {
				setText(last.getText());
				lineTracker.set(last.getText());
			}
		}
	}

	/**
	 * Set the text content from a String. Converts to ImmutableCharSequence
	 * internally.
	 *
	 * @param text the new text content
	 */
	private void setText(String text) {
		myText = CharArrayUtil.createImmutableCharSequence(text);
	}


	public int getTextLength() {
		return getTextSequence().length();
	}

	/**
	 * The text document's uri.
	 */
	@NonNull
	public String getUri() {
		return this.uri;
	}

	/**
	 * The text document's uri.
	 */
	public void setUri(@NonNull final String uri) {
		this.uri = Preconditions.checkNotNull(uri, "uri");
	}

	/**
	 * The text document's language identifier
	 */
	@NonNull
	public String getLanguageId() {
		return this.languageId;
	}

	/**
	 * The text document's language identifier
	 */
	public void setLanguageId(@NonNull final String languageId) {
		this.languageId = Preconditions.checkNotNull(languageId, "languageId");
	}

	/**
	 * The version number of this document (it will strictly increase after each
	 * change, including undo/redo).
	 */
	public int getVersion() {
		return this.version;
	}

	/**
	 * The version number of this document (it will strictly increase after each
	 * change, including undo/redo).
	 */
	public void setVersion(final int version) {
		this.version = version;
	}

	/**
	 * The content of the opened text document.
	 */
	public CharSequence getTextSequence() {
		return myText;
	}

	/**
	 * The content of the opened text document.
	 */
	public String getText() {
		return myText.toString();
	}

	public void replaceString(int startOffset, int endOffset, int moveOffset, CharSequence s,
			long newModificationStamp, boolean wholeTextReplaced) {
		assertBounds(startOffset, endOffset);
		
		if (moveOffset != startOffset && startOffset != endOffset && s.length() != 0) {
			throw new IllegalArgumentException(
					"moveOffset != startOffset for a modification which is neither an insert nor deletion."
							+ " startOffset: " + startOffset + "; endOffset: " + endOffset + ";" + "; moveOffset: "
							+ moveOffset + ";");
		}

		int initialStartOffset = startOffset;
		int initialOldLength = endOffset - startOffset;

		int newStringLength = s.length();
		CharSequence chars = myText;
		int newStartInString = 0;
		while (newStartInString < newStringLength && startOffset < endOffset
				&& s.charAt(newStartInString) == chars.charAt(startOffset)) {
			startOffset++;
			newStartInString++;
		}
		if (newStartInString == newStringLength && startOffset == endOffset && !wholeTextReplaced) {
			return;
		}

		int newEndInString = newStringLength;
		while (endOffset > startOffset && newEndInString > newStartInString
				&& s.charAt(newEndInString - 1) == chars.charAt(endOffset - 1)) {
			newEndInString--;
			endOffset--;
		}

		if (startOffset == 0 && endOffset == getTextLength()) {
			wholeTextReplaced = true;
		}

		CharSequence changedPart = s.subSequence(newStartInString, newEndInString);
		CharSequence sToDelete = myText.subtext(startOffset, endOffset);
		/*boolean isForceIgnoreGuardsOnFullUpdate = getUserData(IGNORE_RANGE_GUARDS_ON_FULL_UPDATE) == Boolean.TRUE
				&& wholeTextReplaced;
		if (!isForceIgnoreGuardsOnFullUpdate) {
			RangeMarker guard = getRangeGuard(startOffset, endOffset);
			if (guard != null) {
				throwGuardedFragment(guard, startOffset, sToDelete, changedPart);
			}
		}*/

		ImmutableCharSequence newText;
		if (wholeTextReplaced && s instanceof ImmutableCharSequence) {
			newText = (ImmutableCharSequence) s;
		} else {
			newText = myText.replace(startOffset, endOffset, changedPart);
			if (!(changedPart instanceof String)) {
				changedPart = newText.subtext(startOffset, startOffset + changedPart.length());
			}
		}
		boolean wasOptimized = initialStartOffset != startOffset || endOffset - startOffset != initialOldLength;
		updateText(newText, startOffset, sToDelete, changedPart, wholeTextReplaced, newModificationStamp,
				initialStartOffset, initialOldLength, wasOptimized ? startOffset : moveOffset);
		//trimToSize();
	}

	private void assertBounds(int startOffset, int endOffset) {
		if (startOffset < 0 || startOffset > getTextLength()) {
			throw new IndexOutOfBoundsException(
					"Wrong startOffset: " + startOffset + "; documentLength: " + getTextLength());
		}
		if (endOffset < 0 || endOffset > getTextLength()) {
			throw new IndexOutOfBoundsException(
					"Wrong endOffset: " + endOffset + "; documentLength: " + getTextLength());
		}
		if (endOffset < startOffset) {
			throw new IllegalArgumentException("endOffset < startOffset: " + endOffset + " < " + startOffset
					+ "; documentLength: " + getTextLength());
		}
	}
	
	private void updateText(ImmutableCharSequence newText, int offset, CharSequence oldString, CharSequence newString, boolean wholeTextReplaced, long newModificationStamp, int initialStartOffset, int initialOldLength, int moveOffset) {
       

       // if (LOG.isTraceEnabled()) {
       //     LOG.trace("updating document " + this + ".\nNext string:'" + newString + "'\nOld string:'" + oldString + "'");
       // }

        //assert moveOffset >= 0 && moveOffset <= this.getTextLength() : "Invalid moveOffset: " + moveOffset;

        /*this.assertNotNestedModification();
        this.myChangeInProgress = true;
        DelayedExceptions exceptions = new DelayedExceptions();

        try {
            DocumentEvent event = new DocumentEventImpl(this, offset, oldString, newString, this.myModificationStamp, wholeTextReplaced, initialStartOffset, initialOldLength, moveOffset);
            this.beforeChangedUpdate(event, exceptions);
            this.myTextString = null;
            ImmutableCharSequence prevText = this.myText;*/
            this.myText = newText;
/*            this.sequence.incrementAndGet();
            this.changedUpdate(event, newModificationStamp, prevText, exceptions);
        } finally {
            this.myChangeInProgress = false;
            exceptions.rethrowPCE();
        }
*/
    }

}
