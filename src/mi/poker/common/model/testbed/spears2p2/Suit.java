package mi.poker.common.model.testbed.spears2p2;


/* Internal suit representation
 * ordinal = 0 -> c
 * ordinal = 1 -> d
 * ordinal = 2 -> h
 * ordinal = 3 -> s
 */

public enum Suit {
	Clubs("c"),
	Diamonds("d"),
	Hearts("h"),
	Spades("s");
	
	private final String toString;
	
	private Suit(String toString) {
		this.toString = toString;
	}
	
        /**
         * 
         * @param 
         * @return c, d, h or s
         */
	public String toString() {
		return toString;
	}
	
        /**
         * 
         * @param s
         * @return the suit corresponding to the parameter, if recognized
         */
	public static Suit parse(String s)  {
		for (Suit suit : Suit.values()) {
			if(suit.toString.equalsIgnoreCase(s)) return suit;
		}
		throw new RuntimeException("Unrecognized suit: " + s);
	}

}
