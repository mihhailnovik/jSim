package mi.poker.common.model.testbed.spears2p2;

/**
<PRE>Internal card representation </p>
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
 </PRE>
 */
public class Card {
    
    public static final int count = 52;
    private static Card[] values = new Card[count];
    public final Rank rank;
    public final Suit suit;
    int ordinal;
    final String toString;
    final String name;
	
    static {
        for (Rank rank : Rank.values()) {
                for (Suit suit : Suit.values()) {
                        Card c = new Card(rank, suit);
                        int ordinal = c.ordinal;
                        values[ordinal] = c;
                }
        }
    }

    private Card(Rank rank, Suit suit) {
            this.rank = rank;
            this.suit = suit;
            this.ordinal = ordinal(rank, suit);
            this.name = rank.name() + " Of " + suit.name();
            this.toString = rank.toString() + suit.toString();
    }
	
    private static int ordinal(Rank rank, Suit suit) {
        // spears: changed from rank.ordinal() + suit.ordinal() * 13
        return rank.ordinal() * 4 + suit.ordinal();  
	}

  /**
   * 
   * @param s valid string of a list of cards, e.g AdKc2h
   * @return an array of cards. Throws a RuntimeException if the parameter is not valid.
   */
    public static Card[] parseArray(String s) {
            int noCards = s.length()/2;
            Card[] result = new Card[noCards];
            for (int i = 0; i < noCards; i++) {
                    result[i] = Card.parse( s.substring(2*i, 2*i+2));
            }
            return result;
    }

  /**
   * 
   * @param s the short description of the card, e.g As, Td or 2h.
   * @return the card. Throws a RuntimeException if the parameter does not correspond to a valid card.
   */
    public static Card parse(String s) {
    	Rank rank = Rank.parse(s.substring(0, 1));
    	Suit suit = Suit.parse(s.substring(1, 2));

    	return Card.get(rank, suit);
    }
    
    public static Card get(Rank rank, Suit suit) {
            return Card.values()[ordinal(rank, suit)];
	}

    public static Card get(int ord) {
            return values[ord];
        }
  

    public static Card[] values() {
		return values;
	} 

  /**
   * 
   * @param hand short descriptions of an array of cards
   * @return the concatenated string
   */
    public static String toString(Card[] hand) {
            if(hand == null) return "NULL";
            if(hand.length == 0)return "NULL";
            StringBuffer b = new StringBuffer();
            for (Card card : hand) {
                    b.append(card.toString);
            }
            return b.toString();
    }

	
  /**
   * 
   * @return the integer description of the card. Ranges from 0 to 51
   */
    public int ordinal() {
            return ordinal;
    }
	
/**
   * 
   * @return the short description of the card, like Ad or 9s
   */
    public String toString() {
            return toString;
    }
	
/**
         * 
         * @return the long litteral description of the card, eg Ace Of Spades
         */
    public String name() {
            return name;
    }
	
}



