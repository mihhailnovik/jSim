package mi.poker.common.model.testbed.spears2p2;

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

public enum Rank {
	Deuce	("2"), 
	Three	("3"), 
	Four	("4"), 
	Five	("5"), 
	Six		("6"),
	Seven	("7"), 
	Eight	("8"), 
	Nine	("9"), 
	Ten		("T"), 
	Jack	("J"), 
	Queen	("Q"), 
	King	("K"), 
	Ace		("A");
	
	private final String toString;


	private Rank(String toString) {
		this.toString = toString;
	}
	
        /**
         * 
         * @param 
         * @return 2, 3,..., T, J,...
         */
        public String toString() {
		return toString;
	}
	
        /**
         * 
         * @param s
         * @return the rank corresponding to the parameter, if recognized
         */
        public static Rank parse(String s)  {
		for (Rank r : Rank.values()) {
			if(s.equalsIgnoreCase(r.toString)) return r;
		}
		throw new RuntimeException("Unrecognized rank: " + s);
	}
	

}
