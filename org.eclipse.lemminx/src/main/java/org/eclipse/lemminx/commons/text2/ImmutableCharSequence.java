// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.eclipse.lemminx.commons.text2;

public abstract class ImmutableCharSequence implements CharSequence {

  public static CharSequence asImmutable(final  CharSequence cs) {
    return isImmutable(cs) ? cs : cs.toString();
  }

  private static boolean isImmutable(final  CharSequence cs) {
    return cs instanceof ImmutableCharSequence ||
           cs instanceof CharSequenceSubSequence && isImmutable(((CharSequenceSubSequence)cs).getBaseSequence());
  }

  
  public abstract  ImmutableCharSequence concat( CharSequence sequence);

  
  public abstract  ImmutableCharSequence insert(int index,  CharSequence seq);

  
  public abstract  ImmutableCharSequence delete(int start, int end);

  
  public abstract  ImmutableCharSequence subtext(int start, int end);

  
  public  ImmutableCharSequence replace(int start, int end,  CharSequence seq) {
    return delete(start, end).insert(start, seq);
  }

  @Override
  public abstract  String toString();
}