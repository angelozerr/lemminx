/**
 *  Copyright (c) 2024 Angelo ZERR.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Memory-efficient CharSequence implementation that stores text in segments.
 * Inspired by IntelliJ IDEA's approach to handle large files.
 * 
 * Benefits:
 * - Reduces memory spikes during incremental updates
 * - Shares unchanged segments between versions
 * - Avoids full text duplication
 * 
 * @author Angelo ZERR
 */
public class SegmentedCharSequence implements CharSequence {
	
	private static final int SEGMENT_SIZE = 8192; // 8KB segments
	
	private final List<String> segments;
	private final int length;
	
	/**
	 * Create from a String by splitting into segments
	 */
	public SegmentedCharSequence(String text) {
		this.length = text.length();
		this.segments = new ArrayList<>((length / SEGMENT_SIZE) + 1);
		
		for (int i = 0; i < length; i += SEGMENT_SIZE) {
			int end = Math.min(i + SEGMENT_SIZE, length);
			segments.add(text.substring(i, end));
		}
	}
	
	/**
	 * Create from existing segments (for structural sharing)
	 */
	private SegmentedCharSequence(List<String> segments, int length) {
		this.segments = segments;
		this.length = length;
	}
	
	@Override
	public int length() {
		return length;
	}
	
	@Override
	public char charAt(int index) {
		if (index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
		}
		
		int segmentIndex = index / SEGMENT_SIZE;
		int offsetInSegment = index % SEGMENT_SIZE;
		return segments.get(segmentIndex).charAt(offsetInSegment);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		if (start < 0 || end > length || start > end) {
			throw new IndexOutOfBoundsException("start: " + start + ", end: " + end + ", length: " + length);
		}
		
		// For small subsequences, just return a String
		if (end - start < SEGMENT_SIZE) {
			return toString().substring(start, end);
		}
		
		// For large subsequences, create a new segmented sequence
		int startSegment = start / SEGMENT_SIZE;
		int endSegment = (end - 1) / SEGMENT_SIZE;
		
		List<String> newSegments = new ArrayList<>(endSegment - startSegment + 1);
		
		for (int i = startSegment; i <= endSegment; i++) {
			String segment = segments.get(i);
			int segmentStart = (i == startSegment) ? start % SEGMENT_SIZE : 0;
			int segmentEnd = (i == endSegment) ? ((end - 1) % SEGMENT_SIZE) + 1 : segment.length();
			
			if (segmentStart == 0 && segmentEnd == segment.length()) {
				// Share the entire segment
				newSegments.add(segment);
			} else {
				// Need to create a substring
				newSegments.add(segment.substring(segmentStart, segmentEnd));
			}
		}
		
		return new SegmentedCharSequence(newSegments, end - start);
	}
	
	@Override
	public String toString() {
		if (segments.size() == 1) {
			return segments.get(0);
		}
		
		StringBuilder sb = new StringBuilder(length);
		for (String segment : segments) {
			sb.append(segment);
		}
		return sb.toString();
	}
	
	/**
	 * Replace a range with new text, sharing unchanged segments
	 */
	public SegmentedCharSequence replace(int start, int end, String replacement) {
		if (start < 0 || end > length || start > end) {
			throw new IndexOutOfBoundsException("start: " + start + ", end: " + end + ", length: " + length);
		}
		
		int newLength = length - (end - start) + replacement.length();
		List<String> newSegments = new ArrayList<>();
		
		// Calculate which segments are affected
		int startSegment = start / SEGMENT_SIZE;
		int endSegment = (end > 0) ? ((end - 1) / SEGMENT_SIZE) : 0;
		
		// Copy unchanged segments before the modification
		for (int i = 0; i < startSegment; i++) {
			newSegments.add(segments.get(i));
		}
		
		// Build the modified region
		StringBuilder modifiedRegion = new StringBuilder();
		
		// Add the prefix from the start segment
		if (startSegment < segments.size()) {
			String startSeg = segments.get(startSegment);
			int offsetInStartSeg = start % SEGMENT_SIZE;
			if (offsetInStartSeg > 0) {
				modifiedRegion.append(startSeg, 0, offsetInStartSeg);
			}
		}
		
		// Add the replacement text
		modifiedRegion.append(replacement);
		
		// Add the suffix from the end segment
		if (endSegment < segments.size()) {
			String endSeg = segments.get(endSegment);
			int offsetInEndSeg = end % SEGMENT_SIZE;
			if (offsetInEndSeg < endSeg.length()) {
				modifiedRegion.append(endSeg, offsetInEndSeg, endSeg.length());
			}
		}
		
		// Split the modified region into segments
		String modifiedText = modifiedRegion.toString();
		for (int i = 0; i < modifiedText.length(); i += SEGMENT_SIZE) {
			int segEnd = Math.min(i + SEGMENT_SIZE, modifiedText.length());
			newSegments.add(modifiedText.substring(i, segEnd));
		}
		
		// Copy unchanged segments after the modification
		for (int i = endSegment + 1; i < segments.size(); i++) {
			newSegments.add(segments.get(i));
		}
		
		return new SegmentedCharSequence(newSegments, newLength);
	}
	
	/**
	 * Get memory footprint estimate in bytes
	 */
	public long getMemoryFootprint() {
		long total = 0;
		for (String segment : segments) {
			// String overhead + char array (2 bytes per char)
			total += 40 + (segment.length() * 2);
		}
		return total;
	}
}

// Made with Bob
