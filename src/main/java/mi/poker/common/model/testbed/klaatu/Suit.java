/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mi.poker.common.model.testbed.klaatu;

public enum Suit {CLUB, DIAMOND, HEART, SPADE;

/**
* The card suits, from club to spade.
* Internal suit representation :
* ordinal = 0 -> c
* ordinal = 1 -> d
* ordinal = 2 -> h
* ordinal = 3 -> s
*/

    /**
        * @return the character in {@link #SUIT_CHARS} denoting this suit.
        */
    public char toChar() {
        return SUIT_CHARS.charAt(this.ordinal());
    }

    /**
        * @param c a character present in {@link #SUIT_CHARS} (case insensitive)
        * @return the Suit denoted by the character.
        * @throws IllegalArgumentException if c not in {@link #SUIT_CHARS}
        */
    public static Suit fromChar(char c) {

        int i = SUIT_CHARS.indexOf(Character.toLowerCase(c));
        if (i >= 0)
            return  Suit.values()[i];
        throw new IllegalArgumentException("'" + c + "'");
    }

    public static final String SUIT_CHARS = "cdhs";
}
