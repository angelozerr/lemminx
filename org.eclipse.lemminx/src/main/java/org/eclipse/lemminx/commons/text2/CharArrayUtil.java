// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.eclipse.lemminx.commons.text2;

import java.io.Reader;
//import java.nio.CharBuffer;

public final class CharArrayUtil {
  private CharArrayUtil() {
  }

  /**
   * Copies all symbols from the given char sequence to the given array
   *
   * @param src         source data holder
   * @param dst         output data buffer
   * @param dstOffset   start offset to use within the given output data buffer
   */
  public static void getChars( CharSequence src, char  [] dst, int dstOffset) {
    getChars(src, dst, dstOffset, src.length());
  }

  /**
   * Copies necessary number of symbols from the given char sequence start to the given array.
   *
   * @param src         source data holder
   * @param dst         output data buffer
   * @param dstOffset   start offset to use within the given output data buffer
   * @param len         number of source data symbols to copy to the given buffer
   */
  public static void getChars( CharSequence src, char  [] dst, int dstOffset, int len) {
    getChars(src, dst, 0, dstOffset, len);
  }

  /**
   * Copies the necessary number of symbols from the given char sequence to the given array.
   *
   * @param src         source data holder
   * @param dst         output data buffer
   * @param srcOffset   source text offset
   * @param dstOffset   start offset to use within the given output data buffer
   * @param len         number of source data symbols to copy to the given buffer
   */
  public static void getChars( CharSequence src, char  [] dst, int srcOffset, int dstOffset, int len) {
	  if (src instanceof CharArrayExternalizable) {
          ((CharArrayExternalizable)src).getChars(srcOffset, srcOffset + len, dst, dstOffset);
      } else {
          if (len >= 10) {
              if (src instanceof String) {
                  ((String)src).getChars(srcOffset, srcOffset + len, dst, dstOffset);
                  return;
              }

             /* if (src instanceof CharBuffer) {
                  CharBuffer buffer = (CharBuffer)src;
                  int i = buffer.position();
                  buffer.position(i + srcOffset);
                  buffer.get(dst, dstOffset, len);
                  buffer.position(i);
                  return;
              }*/

              if (src instanceof CharSequenceBackedByArray) {
                  ((CharSequenceBackedByArray)src.subSequence(srcOffset, srcOffset + len)).getChars(dst, dstOffset);
                  return;
              }

              if (src instanceof StringBuffer) {
                  ((StringBuffer)src).getChars(srcOffset, srcOffset + len, dst, dstOffset);
                  return;
              }

              if (src instanceof StringBuilder) {
                  ((StringBuilder)src).getChars(srcOffset, srcOffset + len, dst, dstOffset);
                  return;
              }
          }

          int i = 0;
          int j = srcOffset;

          for(int max = srcOffset + len; j < max && i < dst.length; ++j) {
              dst[i + dstOffset] = src.charAt(j);
              ++i;
          }

      }
  }

  /**
   * @return if a CharSequence wraps a char[] array and provides direct access to this array -> returns the wrapped array,
   * returns null otherwise
   *
   * @see CharSequenceBackedByArray
   * @see java.nio.CharBuffer#array()
   */
  public static char  [] fromSequenceWithoutCopying( CharSequence seq) {
      if (seq instanceof CharSequenceBackedByArray) {
          return ((CharSequenceBackedByArray)seq).getChars();
      } else {
          /*if (seq instanceof CharBuffer) {
              CharBuffer buffer = (CharBuffer)seq;
              if (buffer.hasArray() && !buffer.isReadOnly() && buffer.arrayOffset() == 0 && buffer.position() == 0) {
                  return buffer.array();
              }
          }*/

          return null;
      }
  }


  /**
   * @return a new char array containing the subsequence's chars
   */
  public static char  [] fromSequence( CharSequence seq, int start, int end) {
	  char[] result = new char[end - start];
      getChars(seq, result, start, 0, end - start);
      return result;
  }
  
  public static int shiftForwardCarefully( CharSequence buffer, int offset,  String chars) {
    if (offset + 1 >= buffer.length()) return offset;
    if (!isSuitable(chars, buffer.charAt(offset))) return offset;
    offset++;
    while (true) {
      if (offset >= buffer.length()) return offset - 1;
      char c = buffer.charAt(offset);
      if (!isSuitable(chars, c)) return offset - 1;
      offset++;
    }
  }

  private static boolean isSuitable( String chars, char c) {
    for (int i = 0; i < chars.length(); i++) {
      if (c == chars.charAt(i)) return true;
    }
    return false;
  }

  public static int shiftBackward(char  [] buffer, int offset,  String chars) {
    return shiftBackward(new CharArrayCharSequence(buffer), offset, chars);
  }

  public static int shiftBackward( CharSequence buffer, int offset,  String chars) {
    return shiftBackward(buffer, 0, offset, chars);
  }

  /**
   * @return minimal offset in the {@code minOffset}-{@code maxOffset}  range after which {@code buffer} contains only characters from
   * {@code chars} in the range
   */
  public static int shiftBackward( CharSequence buffer, int minOffset, int maxOffset,  String chars) {
    if (maxOffset >= buffer.length()) return maxOffset;

    int offset = maxOffset;
    while (true) {
      if (offset < minOffset) break;
      char c = buffer.charAt(offset);
      int i;
      for (i = 0; i < chars.length(); i++) {
        if (c == chars.charAt(i)) break;
      }
      if (i == chars.length()) break;
      offset--;
    }
    return offset;
  }



  public static boolean regionMatches(char  [] buffer, int start, int end,  CharSequence s) {
    int len = s.length();
    if (start + len > end) return false;
    if (start < 0) return false;
    for (int i = 0; i < len; i++) {
      if (buffer[start + i] != s.charAt(i)) return false;
    }
    return true;
  }

  private static void assertRegionIndicesInRange(int s1Length, int start1, int end1,
                                                 int s2Length, int start2, int end2) {
    if (start1 < 0 || start1 > end1 || end1 > s1Length || start2 < 0 || start2 > end2 || end2 > s2Length) {
      throw new IllegalArgumentException("Indices out of bounds: (" + start1 + ", " + end1 + ") of CharSequence length " + s1Length +
                                         " vs (" + start2 + ", " + end2 + ") of CharSequence length " + s2Length);
    }
  }

  public static boolean regionMatches( CharSequence s1, int start1, int end1,  CharSequence s2, int start2, int end2) {
    if (end1 - start1 != end2 - start2) return false;
    assertRegionIndicesInRange(s1.length(), start1, end1, s2.length(), start2, end2);

    for (int i = start1, j = start2; i < end1; i++, j++) {
      if (s1.charAt(i) != s2.charAt(j)) return false;
    }
    return true;
  }
 
  public static boolean regionMatches(CharSequence buffer, int offset, CharSequence s) {
      if (offset + s.length() > buffer.length()) {
          return false;
      } else if (offset < 0) {
          return false;
      } else {
          for(int i = 0; i < s.length(); ++i) {
              if (buffer.charAt(offset + i) != s.charAt(i)) {
                  return false;
              }
          }

          return true;
      }
  }


  public static boolean equals(char  [] buffer1, int start1, int end1, char  [] buffer2, int start2, int end2) {
    if (end1 - start1 != end2 - start2) return false;
    for (int i = start1; i < end1; i++) {
      if (buffer1[i] != buffer2[i - start1 + start2]) return false;
    }
    return true;
  }

  public static int indexOf(char  [] buffer,  String pattern, int fromIndex) {
    char[] chars = pattern.toCharArray();
    int limit = buffer.length - chars.length + 1;
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    SearchLoop:
    for (int i = fromIndex; i < limit; i++) {
      for (int j = 0; j < chars.length; j++) {
        if (chars[j] != buffer[i + j]) continue SearchLoop;
      }
      return i;
    }
    return -1;
  }

  public static int indexOf( CharSequence buffer,  CharSequence pattern, int fromIndex) {
    return indexOf(buffer, pattern, fromIndex, buffer.length());
  }

  /**
   * Tries to find index of a given pattern at the given buffer.
   *
   * @param buffer       characters buffer which contents should be checked for the given pattern
   * @param pattern      target characters sequence to find at the given buffer
   * @param fromIndex    start index (inclusive). Zero is used if given index is negative
   * @param toIndex      end index (exclusive)
   * @return             index of the given pattern at the given buffer if the match is found; {@code -1} otherwise
   */
  public static int indexOf( CharSequence buffer,  CharSequence pattern, int fromIndex, int toIndex) {
    int patternLength = pattern.length();
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    int limit = toIndex - patternLength + 1;
    SearchLoop:
    for (int i = fromIndex; i < limit; i++) {
      for (int j = 0; j < patternLength; j++) {
        if (pattern.charAt(j) != buffer.charAt(i + j)) continue SearchLoop;
      }
      return i;
    }
    return -1;
  }

  /**
   * Tries to find index that points to the first location of the given symbol at the given char array at range {@code [from; to)}.
   *
   * @param buffer      target symbols holder to check
   * @param symbol      target symbol which offset should be found
   * @param fromIndex   start index to search (inclusive)
   * @param toIndex     end index to search (exclusive)
   * @return            index that points to the first location of the given symbol at the given char array at range
   *                    {@code [from; to)} if target symbol is found;
   *                    {@code -1} otherwise
   */
  public static int indexOf(char  [] buffer, char symbol, int fromIndex, int toIndex) {
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    for (int i = fromIndex; i < toIndex; i++) {
      if (buffer[i] == symbol) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Tries to find index that points to the last location of the given symbol at the given char array at range {@code [from; to)}.
   *
   * @param buffer      target symbols holder to check
   * @param symbol      target symbol which offset should be found
   * @param fromIndex   start index to search (inclusive)
   * @param toIndex     end index to search (exclusive)
   * @return            index that points to the last location of the given symbol at the given char array at range
   *                    {@code [from; to)} if target symbol is found;
   *                    {@code -1} otherwise
   */
  public static int lastIndexOf(char  [] buffer, char symbol, int fromIndex, int toIndex) {
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    for (int i = toIndex - 1; i >= fromIndex; i--) {
      if (buffer[i] == symbol) {
        return i;
      }
    }
    return -1;
  }

  public static int lastIndexOf( CharSequence buffer,  String pattern, int maxIndex) {
    char[] chars = pattern.toCharArray();
    int end = buffer.length() - chars.length;
    if (maxIndex > end) {
      maxIndex = end;
    }
    SearchLoop:
    for (int i = maxIndex; i >= 0; i--) {
      for (int j = 0; j < chars.length; j++) {
        if (chars[j] != buffer.charAt(i + j)) continue SearchLoop;
      }
      return i;
    }
    return -1;
  }

  public static int lastIndexOf(char  [] buffer,  String pattern, int maxIndex) {
    char[] chars = pattern.toCharArray();
    int end = buffer.length - chars.length;
    if (maxIndex > end) {
      maxIndex = end;
    }
    SearchLoop:
    for (int i = maxIndex; i >= 0; i--) {
      for (int j = 0; j < chars.length; j++) {
        if (chars[j] != buffer[i + j]) continue SearchLoop;
      }
      return i;
    }
    return -1;
  }

  public static boolean containsOnlyWhiteSpaces( CharSequence chars) {
    if (chars == null) return true;
    for (int i = 0; i < chars.length(); i++) {
      char c = chars.charAt(i);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r') continue;
      return false;
    }
    return true;
  }

  /**
   * Allows answering if a target region of the given text contains only white space symbols (tabulations, white spaces, and line feeds).
   *
   * @param text      text to check
   * @param start     start offset within the given text to check (inclusive)
   * @param end       end offset within the given text to check (exclusive)
   * @return          {@code true} if a target region of the given text contains white space symbols only; {@code false} otherwise
   */
  public static boolean isEmptyOrSpaces( CharSequence text, int start, int end) {
    for (int i = start; i < end; i++) {
      char c = text.charAt(i);
      if (c != ' ' && c != '\t' && c != '\n') {
        return false;
      }
    }
    return true;
  }

  /**
   * Make a {@link Reader} from given {@link CharSequence}.
   * If {@link CharSequence} wraps a char[], and provides access to the underlying buffer -- wraps this char[] for faster access,
   * otherwise just wraps given {@link CharSequence}
   *
   * @see CharSequenceBackedByArray
   * @see java.nio.CharBuffer#array()
   */
  public static  Reader readerFromCharSequence( CharSequence text) {
    char[] chars = fromSequenceWithoutCopying(text);
    return chars == null ? new CharSequenceReader(text) : null;//new UnsyncCharArrayReader(chars, 0, text.length());
  }

  //TODO: move to a better place or inline, because it creates excessive dependencies
  public static  ImmutableCharSequence createImmutableCharSequence( CharSequence sequence) {
    return ImmutableText.valueOf(sequence);
  }
}