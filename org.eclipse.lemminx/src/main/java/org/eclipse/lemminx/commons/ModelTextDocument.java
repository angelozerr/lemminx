/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * A {@link TextDocument} which is associate to a model loaded in async.
 *
 * @author Angelo ZERR
 *
 * @param <T> the model type (ex : DOM Document)
 */
public class ModelTextDocument<T> extends TextDocument {

	private static final Logger LOGGER = Logger.getLogger(ModelTextDocument.class.getName());

	private final BiFunction<TextDocument, CancelChecker, T> parse;

	private volatile T model;

	private volatile T previousModel;

	private volatile EditInfo pendingEdit;

	public ModelTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, T> parse) {
		super(document);
		this.parse = parse;
	}

	public ModelTextDocument(String text, String uri, BiFunction<TextDocument, CancelChecker, T> parse) {
		super(text, uri);
		this.parse = parse;
	}

	/**
	 * Returns the existing parsed model synchronized with last version of the text
	 * document and null otherwise.
	 *
	 * @return the existing parsed model synchronized with last version of the text
	 *         document and null otherwise.
	 */
	public T getExistingModel() {
		return model;
	}

	/**
	 * Returns the parsed model synchronized with last version of the text document.
	 *
	 * @return the parsed model synchronized with last version of the text document.
	 */
	public T getModel() {
		if (model == null) {
			return getSynchronizedModel();
		}
		return model;
	}

	/**
	 * Return the existing parsed model synchronized with last version of the text
	 * document or parse the model.
	 *
	 * @return the existing parsed model synchronized with last version of the text
	 *         document or parse the model.
	 */
	private synchronized T getSynchronizedModel() {
		if (model != null) {
			return model;
		}
		int version = super.getVersion();
		long start = System.currentTimeMillis();
		try {
			LOGGER.fine("Start parsing of model with version '" + version);
			// Stop of parse process can be done when completable future is canceled or when
			// version of document changes
			CancelChecker cancelChecker = new TextDocumentVersionChecker(this, version);
			// parse the model
			model = parse.apply(this, cancelChecker);
		} catch (CancellationException e) {
			LOGGER.fine("Stop parsing parsing of model with version '" + version + "' in "
					+ (System.currentTimeMillis() - start) + "ms");
			throw e;
		} finally {
			previousModel = null;
			pendingEdit = null;
			LOGGER.fine("End parse of model with version '" + version + "' in " + (System.currentTimeMillis() - start)
					+ "ms");
		}
		return model;
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		// text changed, cancel the completable future which load the model
		cancelModel();
	}

	@Override
	public void setVersion(int version) {
		super.setVersion(version);
		// version changed, mark the model as dirty
		cancelModel();
	}

	@Override
	public void update(List<TextDocumentContentChangeEvent> changes) {
		if (changes != null && changes.size() == 1) {
			TextDocumentContentChangeEvent change = changes.get(0);
			Range range = change.getRange();
			if (range != null) {
				try {
					int start = offsetAt(range.getStart());
					Integer rangeLength = change.getRangeLength();
					int delLen = rangeLength != null ? rangeLength.intValue()
							: offsetAt(range.getEnd()) - start;
					int insLen = change.getText() != null ? change.getText().length() : 0;
					pendingEdit = new EditInfo(start, delLen, insLen);
				} catch (BadLocationException e) {
					pendingEdit = null;
				}
			}
		} else {
			pendingEdit = null;
		}
		super.update(changes);
	}

	/**
	 * Mark the model as dirty
	 */
	private void cancelModel() {
		if (model != null) {
			previousModel = model;
		}
		model = null;
	}

	/**
	 * Returns the previous model (before the last edit) and null if not available.
	 *
	 * @return the previous model or null
	 */
	public T getPreviousModel() {
		return previousModel;
	}

	/**
	 * Returns the pending edit info (offset, delete/insert lengths) for
	 * the most recent single-change edit, or null if not available.
	 *
	 * @return the edit info or null
	 */
	public EditInfo getPendingEdit() {
		return pendingEdit;
	}

	/**
	 * Information about a single text edit: where it started in the old text,
	 * how many characters were deleted, and how many were inserted.
	 */
	public static final class EditInfo {
		private final int startOffset;
		private final int deleteLength;
		private final int insertLength;

		public EditInfo(int startOffset, int deleteLength, int insertLength) {
			this.startOffset = startOffset;
			this.deleteLength = deleteLength;
			this.insertLength = insertLength;
		}

		public int getStartOffset() {
			return startOffset;
		}

		public int getDeleteLength() {
			return deleteLength;
		}

		public int getInsertLength() {
			return insertLength;
		}
	}

}
