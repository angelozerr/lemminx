package org.eclipse.lemminx.commons.text2;

public class Strings {

	public static int stringHashCode(CharSequence chars) {
		return !(chars instanceof String) && !(chars instanceof CharSequenceWithStringHash)
				? stringHashCode((CharSequence) chars, 0, chars.length())
				: chars.hashCode();
	}

	public static int stringHashCode(CharSequence chars, int from, int to) {
		return stringHashCode(chars, from, to, 0);
	}

	public static int stringHashCode(CharSequence chars, int from, int to, int prefixHash) {
		int h = prefixHash;

		for (int off = from; off < to; ++off) {
			h = 31 * h + chars.charAt(off);
		}

		return h;
	}

	public static int stringHashCode(char[] chars, int from, int to) {
		int h = 0;

		for (int off = from; off < to; ++off) {
			h = 31 * h + chars[off];
		}

		return h;
	}
}
