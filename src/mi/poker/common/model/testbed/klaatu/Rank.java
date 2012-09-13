/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mi.poker.common.model.testbed.klaatu;

public enum Rank {

/* Internal rank representation
* ordinal = 0 -> 2
* ordinal = 1 -> 3
* ...
* ordinal = 8 -> T
* ordinal = 9 -> J
* ordinal = 10 -> Q
* ordinal = 11 -> K
* ordinal = 12 -> A
*/
    TWO, THREE, FOUR, FIVE, SIX, SEVEN,
    EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

    /**
        * @return the character in {@link #RANK_CHARS} denoting this rank.
        */
    public char toChar() {
        return RANK_CHARS.charAt(this.ordinal());
    }

    /**
        * @param c a character present in {@link #RANK_CHARS} (case insensitive)
        * @return the Rank denoted by character.
        * @throws IllegalArgumentException if c not in {@link #RANK_CHARS}
        */
    public static Rank fromChar(char c) {

        int i = RANK_CHARS.indexOf(Character.toUpperCase(c));
        if (i >= 0)
            return  Rank.values()[i];
        throw new IllegalArgumentException("'" + c + "'");
    }

    /**
        * @return the pip value of this Rank, ranging from 2 for
        *          a <code>TWO</code> (deuce) to 14 for an <code>ACE</code>.
        */
    public int pipValue() {
        return this.ordinal() + 2;
    }

    public static final String RANK_CHARS = "23456789TJQKA";
}
