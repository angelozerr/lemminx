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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for SegmentedCharSequence
 */
public class SegmentedCharSequenceTest {

	@Test
	public void testBasicOperations() {
		String text = "Hello World!";
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		
		assertEquals(text.length(), seq.length());
		assertEquals('H', seq.charAt(0));
		assertEquals('!', seq.charAt(11));
		assertEquals("Hello", seq.subSequence(0, 5).toString());
		assertEquals(text, seq.toString());
	}
	
	@Test
	public void testLargeText() {
		// Create a 100KB text
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			sb.append("0123456789");
		}
		String text = sb.toString();
		
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		assertEquals(text.length(), seq.length());
		assertEquals(text, seq.toString());
		
		// Test random access
		for (int i = 0; i < text.length(); i += 1000) {
			assertEquals(text.charAt(i), seq.charAt(i));
		}
	}
	
	@Test
	public void testReplace() {
		String text = "Hello World!";
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		
		// Replace "World" with "Java"
		SegmentedCharSequence newSeq = seq.replace(6, 11, "Java");
		
		assertEquals("Hello Java!", newSeq.toString());
		assertEquals("Hello World!", seq.toString()); // Original unchanged
	}
	
	@Test
	public void testReplaceInLargeFile() {
		// Simulate editing a large XML file
		StringBuilder sb = new StringBuilder();
		sb.append("<root>\n");
		for (int i = 0; i < 10000; i++) {
			sb.append("  <item id=\"").append(i).append("\">Value ").append(i).append("</item>\n");
		}
		sb.append("</root>");
		
		String originalText = sb.toString();
		SegmentedCharSequence seq = new SegmentedCharSequence(originalText);
		
		long memoryBefore = seq.getMemoryFootprint();
		
		// Modify one item (simulate typing)
		int modifyPos = originalText.indexOf("Value 5000");
		SegmentedCharSequence newSeq = seq.replace(modifyPos, modifyPos + 10, "Modified");
		
		long memoryAfter = newSeq.getMemoryFootprint();
		
		// Memory should not double (structural sharing)
		// Allow some overhead for segment boundaries and new segments
		assertTrue(memoryAfter < memoryBefore * 1.2,
			String.format("Memory increased too much: %d -> %d (%.1f%%)",
				memoryBefore, memoryAfter, (memoryAfter * 100.0 / memoryBefore) - 100));
		
		// Verify the change
		String newText = newSeq.toString();
		assertTrue(newText.contains("Modified"));
		assertFalse(newText.contains("Value 5000"));
	}
	
	@Test
	public void testMultipleReplacements() {
		String text = "AAAA BBBB CCCC DDDD";
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		
		seq = seq.replace(0, 4, "1111");
		assertEquals("1111 BBBB CCCC DDDD", seq.toString());
		
		seq = seq.replace(5, 9, "2222");
		assertEquals("1111 2222 CCCC DDDD", seq.toString());
		
		seq = seq.replace(10, 14, "3333");
		assertEquals("1111 2222 3333 DDDD", seq.toString());
	}
	
	@Test
	public void testInsert() {
		String text = "Hello!";
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		
		// Insert " World" before "!"
		SegmentedCharSequence newSeq = seq.replace(5, 5, " World");
		
		assertEquals("Hello World!", newSeq.toString());
	}
	
	@Test
	public void testDelete() {
		String text = "Hello World!";
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		
		// Delete " World"
		SegmentedCharSequence newSeq = seq.replace(5, 11, "");
		
		assertEquals("Hello!", newSeq.toString());
	}
	
	@Test
	public void testMemoryEfficiency() {
		// Create a 1MB text
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100000; i++) {
			sb.append("0123456789");
		}
		String text = sb.toString();
		
		SegmentedCharSequence seq = new SegmentedCharSequence(text);
		long initialMemory = seq.getMemoryFootprint();
		
		// Make a small change (100 chars)
		SegmentedCharSequence newSeq = seq.replace(50000, 50100, "X".repeat(100));
		long newMemory = newSeq.getMemoryFootprint();
		
		// New memory should be close to initial (structural sharing)
		// Allow 5% overhead for segment boundaries
		assertTrue(newMemory < initialMemory * 1.05,
			String.format("Memory increased too much: %d -> %d", initialMemory, newMemory));
	}
}

// Made with Bob
