//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.lemminx.commons.text2;

public class CharSequenceSubSequence implements CharSequence, CharArrayExternalizable, CharSequenceWithStringHash {
    private final CharSequence myChars;
    private final int myStart;
    private final int myEnd;
    private transient int hash;

    public CharSequenceSubSequence (CharSequence chars) {
        this(chars, 0, chars.length());
    }

    public CharSequenceSubSequence (CharSequence chars, int start, int end) {
        if (start >= 0 && end <= chars.length() && start <= end) {
            this.myChars = chars;
            this.myStart = start;
            this.myEnd = end;
        } else {
            throw new IndexOutOfBoundsException("chars sequence.length:" + chars.length() + ", start:" + start + ", end:" + end);
        }
    }

    public final int length() {
        return this.myEnd - this.myStart;
    }

    public final char charAt(int index) {
        return this.myChars.charAt(index + this.myStart);
    }

    public  CharSequence subSequence(int start, int end) {
        return start == this.myStart && end == this.myEnd ? this : new CharSequenceSubSequence(this.myChars, this.myStart + start, this.myStart + end);
    }

    public  String toString() {
        return this.myChars instanceof String ? ((String)this.myChars).substring(this.myStart, this.myEnd) : new String(CharArrayUtil.fromSequence(this.myChars, this.myStart, this.myEnd));
    }

     CharSequence getBaseSequence() {
        return this.myChars;
    }

    public void getChars(int start, int end, char  [] dest, int destPos) {
        assert end - start <= this.myEnd - this.myStart;
        CharArrayUtil.getChars(this.myChars, dest, start + this.myStart, destPos, end - start);
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            this.hash = h = Strings.stringHashCode(this.myChars, this.myStart, this.myEnd);
        }

        return h;
    }
}
