//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.lemminx.commons.text2;

public class CharArrayCharSequence implements CharSequenceBackedByArray, CharSequenceWithStringHash {
    protected final char[] myChars;
    protected final int myStart;
    protected final int myEnd;
    private transient int hash;

    public CharArrayCharSequence( char... chars) {
        this(chars, 0, chars.length);
    }

    public CharArrayCharSequence(char  [] chars, int start, int end) {
        super();
        if (start >= 0 && end <= chars.length && start <= end) {
            this.myChars = chars;
            this.myStart = start;
            this.myEnd = end;
        } else {
            throw new IndexOutOfBoundsException("chars.length:" + chars.length + ", start:" + start + ", end:" + end);
        }
    }

    public final int length() {
        return this.myEnd - this.myStart;
    }

    public final char charAt(int index) {
        return this.myChars[index + this.myStart];
    }

    public  CharSequence subSequence(int start, int end) {
        return start == 0 && end == this.length() ? this : new CharArrayCharSequence(this.myChars, this.myStart + start, this.myStart + end);
    }

    public  String toString() {
        return new String(this.myChars, this.myStart, this.myEnd - this.myStart);
    }

    public char  [] getChars() {
        if (this.myStart == 0) {
            return this.myChars;
        } else {
            char[] chars = new char[this.length()];
            this.getChars(chars, 0);
            return chars;
        }
    }

    public void getChars(char  [] dst, int dstOffset) {
        System.arraycopy(this.myChars, this.myStart, dst, dstOffset, this.length());
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        } else {
            return anObject != null && this.getClass() == anObject.getClass() && this.length() == ((CharSequence)anObject).length() ? CharArrayUtil.regionMatches(this.myChars, this.myStart, this.myEnd, (CharSequence)anObject) : false;
        }
    }

    public int readCharsTo(int start, char[] cbuf, int off, int len) {
        int readChars = Math.min(len, this.length() - start);
        if (readChars <= 0) {
            return -1;
        } else {
            System.arraycopy(this.myChars, this.myStart + start, cbuf, off, readChars);
            return readChars;
        }
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            this.hash = h = Strings.stringHashCode(this.myChars, this.myStart, this.myEnd);
        }

        return h;
    }
}
