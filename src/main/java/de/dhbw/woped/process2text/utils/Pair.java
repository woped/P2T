/* (C)1999-2005 */
package de.dhbw.woped.process2text.utils;

/**
 * A generic class for pairs.
 *
 * <p><b>This is NOT part of any supported API. If you write code that depends on this, you do so at
 * your own risk. This code and its internal interfaces are subject to change or deletion without
 * notice.</b>
 */
public class Pair<A, B> {
  public final A fst;
  public final B snd;

  public Pair(A fst, B snd) {
    this.fst = fst;
    this.snd = snd;
  }

  private static boolean equals(Object x, Object y) {
    return (x == null && y == null) || (x != null && x.equals(y));
  }

  public String toString() {
    return "Pair[" + fst + "," + snd + "]";
  }

  public boolean equals(Object other) {
    return other instanceof de.dhbw.woped.process2text.utils.Pair<?, ?>
        && equals(fst, ((de.dhbw.woped.process2text.utils.Pair<?, ?>) other).fst)
        && equals(snd, ((de.dhbw.woped.process2text.utils.Pair<?, ?>) other).snd);
  }

  public int hashCode() {
    if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
    else if (snd == null) return fst.hashCode() + 2;
    else return fst.hashCode() * 17 + snd.hashCode();
  }
}
