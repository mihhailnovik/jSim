package mi.poker.common.model.testbed.klaatu;

/**
 * An immutable poker card.
 * @version 2006Dec11.0
 * @author Steve Brecher
 *
 */
public class Card implements Comparable<Card> {
    
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

/* Internal 52 cards storage
 * index = 0 -> 2c
 * index = 1 -> 3c
 * ...
 * index = 12 -> Ac
 * index = 13 -> 2d
 * ...
 * index = 25 -> Ad
 * index = 26 -> 2h
 * ...
 * index = 38 -> Ah
 * index = 39 -> 2s
 * ...
 * index = 51 -> As
 */
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
    
 /**
 * Returns the following i value depending on the card.
 * i = 0  2c; i = 1  2d; i = 2  2h; i = 3  2s; 
 * i = 4  3c; i = 5  3d; i = 6  3h; i = 7  3s; 
 * i = 8  4c; i = 9  4d; i = 10 4h; i = 11 4s; 
 * i = 12 5c; i = 13 5d; i = 14 5h; i = 15 5s; 
 * i = 16 6c; i = 17 6d; i = 18 6h; i = 19 6s; 
 * i = 20 7c; i = 21 7d; i = 22 7h; i = 23 7s; 
 * i = 24 8c; i = 25 8d; i = 26 8h; i = 27 8s; 
 * i = 28 9c; i = 29 9d; i = 30 9h; i = 31 9s; 
 * i = 32 Tc; i = 33 Td; i = 34 Th; i = 35 Ts; 
 * i = 36 Jc; i = 37 Jd; i = 38 Jh; i = 39 Js; 
 * i = 40 Qc; i = 41 Qd; i = 42 Qh; i = 43 Qs; 
 * i = 44 Kc; i = 45 Kd; i = 46 Kh; i = 47 Ks; 
 * i = 48 Ac; i = 49 Ad; i = 50 Ah; i = 51 As;
 */
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
