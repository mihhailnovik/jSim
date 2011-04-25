package mi.poker.common.model.testbed.klaatu;

/**
 * An immutable poker card.
 * @version 2006Dec11.0
 * @author Steve Brecher
 *
 */
public class Card implements Comparable<Card> {
    
    /**
     * The card ranks, from two (deuce) to ace.
     */
    public static enum Rank {
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

    /**
     * The card suits, from club to spade.
     */
    public static enum Suit {CLUB, DIAMOND, HEART, SPADE;
        
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

    private final Rank  rank;
    private final Suit  suit;

    /**
     * Constructs a card of the specified rank and suit.
     * @param rank a {@link Rank}
     * @param suit a {@link Suit}
     * @see #getInstance(com.stevebrecher.poker.Card.Rank, com.stevebrecher.poker.Card.Suit)
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Constructs a card of the specified rank and suit.
     * @param rs a {@link String} of length 2, where the first character is in {@link Card.Rank#RANK_CHARS} and
     *           the second is in {@link Card.Suit#SUIT_CHARS} (case insensitive).
     * @throws IllegalArgumentException on the first character in rs which is not found in the respective string.
     * @see #getInstance(String)
     */
    public Card(String rs) {

        if (rs.length() != 2)
            throw new IllegalArgumentException('"' + rs + "\".length != 2");
        try {
            this.rank = Rank.fromChar(rs.charAt(0));
            this.suit = Suit.fromChar(rs.charAt(1));
        } catch (IllegalArgumentException e) {
            throw e;    // indicates the first erroneous character
        }
    }

    private final static Card[] theCards = new Card[52];
    static {
        int i = 0;
        for (Suit s : Suit.values())
            for (Rank r : Rank.values())
                theCards[i++] = new Card(r, s);
    }
        
    /**
     * Returns a pre-existing instance of {@link Card} of the specified rank and suit.
     * @param rank a {@link Rank}
     * @param suit a {@link Suit}
     * @return an instance of {@link Card} of the specified rank and suit.
     */
    public static Card getInstance(Rank rank, Suit suit) {
        return theCards[suit.ordinal()*13 + rank.ordinal()];
    }

    /**
     * Returns a pre-existing instance of {@link Card} of the specified rank and suit.
     * @param rs a {@link String} of length 2, where the first character is in {@link Card.Rank#RANK_CHARS} and
     *           the second is in {@link Card.Suit#SUIT_CHARS} (case insensitive).
     * @return an instance of {@link Card} of the specified rank and suit.
     * @throws IllegalArgumentException on the first character in rs which is not found in the respective string.
     */
    public static Card getInstance(String rs) {
        if (rs.length() != 2)
            throw new IllegalArgumentException('"' + rs + "\".length != 2");
        try {
            Rank rank = Rank.fromChar(rs.charAt(0));
            Suit suit = Suit.fromChar(rs.charAt(1));
            return theCards[suit.ordinal()*13 + rank.ordinal()];
        } catch (IllegalArgumentException e) {
            throw e;    // indicates the first erroneous character
        }
    }

    /**
     * Returns a {@link String} of length 2 denoting the rank and suit of this card.
     * @return a {@link String} of length 2 containing a character in {@link Card.Rank#RANK_CHARS} denoting this
     *          card&#39;s rank followed by a character in {@link Card.Suit#SUIT_CHARS} denoting this
     *          card&#39;s suit.
     */
    @Override
    public String toString() {
        return String.format("%c%c", rank.toChar(), suit.toChar());
    }
    
    /**
     * Returns the {@link Rank} of this card.
     * @return the {@link Rank} of this card.
     */
    public Rank rankOf() {
        return rank;
    }
    
    /**
     * Returns the {@link Suit} of this card.
     * @return the {@link Suit} of this card.
     */
    public Suit suitOf() {
        return suit;
    }

    /**
     * Compares the parameter to this card.
     * @return <code>true</code> if the parameter is a {@link Card} of the same rank and suit
     *          as this card; <code>false</code> otherwise.
     */
    @Override
    public boolean equals (Object that) {
        if (!(that instanceof Card)) return false;
        Card c = (Card)that;
        return this.rank == c.rank && this.suit == c.suit;
    }
    
    /* result is a perfect hash code */
    @Override
    public int hashCode() {
        return rank.ordinal()*4 + suit.ordinal();
    }

    /**
     * Compares the specified Card to this card, first on rank; then, if equal, on suit.<br>
     * Note that the suit comparison is not germane to the core game of
     * poker but is the one traditionally used to assign stud bring-ins, etc.
     * @param that the Card to be compared
     * @return a negative integer, zero, or a positive integer as this Card is less than,
     *      equal to, or greater than the specified Card.
     */
    public int compareTo(Card that) {
        int result = this.rank.compareTo(that.rank);
        if (result != 0)
            return result;
        return this.suit.compareTo(that.suit);
    }
}
