package mi.poker.common.model.testbed.spears2p2;

import java.io.*;

/**
 * 
 * 
 * http://forumserver.twoplustwo.com/showthreaded.php?Cat=0&Number=9765615&page=0&vc=1
 * http://forumserver.twoplustwo.com/showthreaded.php?Cat=0&Number=9774228&page=0&vc=1
 * 
 * To evaluate 6 card hand make the last card a zero.
 * 
 * 
 *
 */
public class StateTableEvaluator {
	
											  	
	private static final int HAND_RANKS_SIZE = 32487834;
	/* Card to integer conversions:
   		2c =  1    2d =  2    2h =  3    2s =  4
   		3c =  5    3d =  6    3h =  7    3s =  8
   		4c =  9    4d = 10    4h = 11    4s = 12
   		5c = 13    5d = 14    5h = 15    5s = 16
   		6c = 17    6d = 18    6h = 19    6s = 20
   		7c = 21    7d = 22    7h = 23    7s = 24
   		8c = 25    8d = 26    8h = 27    8s = 28
   		9c = 29    9d = 30    9h = 31    9s = 32
   		Tc = 33    Td = 34    Th = 35    Ts = 36
   		Jc = 37    Jd = 38    Jh = 39    Js = 40
   		Qc = 41    Qd = 42    Qh = 43    Qs = 44
   		Kc = 45    Kd = 46    Kh = 47    Ks = 48
   		Ac = 49    Ad = 50    Ah = 51    As = 52
	 */

	private final static int NUM_SUITS = 4;
	private final static int NUM_RANKS = 13;
	
	public static int[]   handRanks			= new int[HAND_RANKS_SIZE]; // array to hold hand rank lookup table
	private static boolean verbose   = true;					// toggles verbose mode
	
	private static int[]  hand; 								// re-usable array to hold cards in a hand
	private static int    size             = 612978;					// lookup table size
	private static long[] keys             = new long[size];	// array to hold key lookup table
	private static int    numKeys          = 1;					// counter for number of defined keys in key array
	private static long   maxKey           = 0;					// holds current maximum key value
	private static int    numCards         = 0;					// re-usable counter for number of cards in a hand
	private static int    cardIndex        = 0;					// re-usable index for cards in a hands
	private static int    maxHandRankIndex = 0;
	
	private static long   startTimer;
	private static long   stopTimer;
	
	private static final String HAND_RANKS_FILE = "handRanks.ser";
	
	public static void initialize() {
		try {
			System.out.println("Loading evaluation tables ...");
			File f = new File(HAND_RANKS_FILE);
			if (!f.exists()) {
				System.out.println("Evaluation tables do not exist, this is first time run. Generating them ...");
				handRanks = new int[HAND_RANKS_SIZE];
				generateTables();
				saveTables();
				System.out.println("Loading evaluation tables (again) ...");
			}
			long t = System.currentTimeMillis();
			ObjectInputStream s = new ObjectInputStream(new FileInputStream(HAND_RANKS_FILE));
			handRanks = (int[])s.readObject();
			t = System.currentTimeMillis() - t;
			System.out.println("Evaluation tables loaded in " + t/1000.0 + " seconds" );
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	

	
	// Inserts a key into the key array and returns the insertion index. 
	private static int insertKey(long key) {
		
		// check to see if key is valid
		if (key == 0) {
			return 0;
		}
		
		// short circuit insertion for most common cases
		if (key >= maxKey) {
			if (key > maxKey) {
				keys[numKeys++] = key;	// appends the new key to the key array
				maxKey = key;
			}
			return numKeys - 1;
		}
		
		// use binary search to find insertion point for new key
		int low = -1;
		int high = numKeys;
		int pivot;
		long difference;
		
		while (high - low > 1) {
			pivot = (low + high) >>> 1;
			difference = keys[pivot] - key;
			if (difference > 0) {
				high = pivot;
			}
			else if (difference < 0) {
				low = pivot;
			}
			else {
				return pivot;	// key already exists
			}
		}
		
		// key does not exist so must be inserted
		System.arraycopy(keys, high, keys, high + 1, numKeys - high);
		keys[high] = key;
		
	
		
		numKeys++;
		return high;
		
	} // END insertKey method
	
	
	
	// Returns a key for the hand created by adding a new card to the hand 
	// represented by the given key.	Returns 0 if new card already appears in hand.
	private static long makeKey(long baseKey, int newCard) {
		
		int[] suitCount = new int[NUM_SUITS + 1]; 	// number of times a suit appears in a hand
		int[] rankCount = new int[NUM_RANKS + 1];	// number of times a rank appears in a hand
		hand = new int[8];										
		
		// extract the hand represented by the key value
		for (cardIndex = 0; cardIndex < 6; cardIndex++) {
			
			// hand[0] is used to hold the new card
			hand[cardIndex + 1] = (int)((baseKey >>> (8 * cardIndex)) & 0xFF);
		}
		
		hand[0] = formatCard8bit(newCard);
		
		// examine the hand to determine number of cards and rank/suit counts
		for (numCards = 0; hand[numCards] != 0; numCards++) {
			suitCount[hand[numCards] & 0xF]++;
			rankCount[(hand[numCards] >>> 4) & 0xF]++;
			
			// check to see if new card is already contained in hand (rank and suit considered)
			if (numCards != 0 && hand[0] == hand[numCards]) {
				return 0;
			}
		}
		
		// check to see if we already have four of a particular rank
		if (numCards > 4) {
			for (int rank = 1; rank < 14; rank++) {
				if (rankCount[rank] > 4) return 0;
			}
		}
		
		// determine the minimum number of suits required for a flush to be possible
		int minSuitCount = numCards - 2;
		
		// check to see if suit is significant
		if (minSuitCount > 1) {
			// examine each card in the hand
			for (cardIndex = 0; cardIndex < numCards; cardIndex++) {
				// if the suit is not significant then strip it from the card
				if (suitCount[hand[cardIndex] & 0xF] < minSuitCount) {
					hand[cardIndex] &= 0xF0;
				}
			}
		}
		
		sortHand();
		
		long key = 0;
		for (int i = 0; i < 7; i++) {
			key += (long)hand[i] << (i * 8);
		}
		
		return key;
		
	} // END makeKey method
	
	
	
	// Formats and returns a card in 8-bit packed representation.
	private static int formatCard8bit(int card) {
		
        // 8-Bit Packed Card Representation
        // +--------+
        // |rrrr--ss|
        // +--------+
        // r = rank of card		(deuce = 1, trey = 2, four = 3, five = 4,..., ace = 13)
	// s = suit of card		(suits are arbitrary, can take value from 0 to 3)	
		    
		card--;
		return (((card >>> 2) + 1) << 4) + (card & 3) + 1;
		
	} // END formatCard8bit method
	
	
	
	// Sorts the hand using Bose-Nelson Sorting Algorithm (N = 7).
	private static void sortHand() {
		swapCard(0, 4);
		swapCard(1, 5);
		swapCard(2, 6);
		swapCard(0, 2);
		swapCard(1, 3);
		swapCard(4, 6);
		swapCard(2, 4);
		swapCard(3, 5);
		swapCard(0, 1);
		swapCard(2, 3);
		swapCard(4, 5);
		swapCard(1, 4);
		swapCard(3, 6);
		swapCard(1, 2);
		swapCard(3, 4);
		swapCard(5, 6);	
	} // End sortHand method
	
	
	
	// Swaps card i with card j.
	private static void swapCard(int i, int j) {
		if (hand[i] < hand[j]) {
			hand[i] ^= hand[j];
			hand[j] ^= hand[i];
			hand[i] ^= hand[j];
		}	
	} // END swapCard method
	
	
	
	// Determines the relative strength of a hand (the hand is given by its unique key value).
	private static int getHandRank(long key) {
		
		// The following method implements a modified version of "Cactus Kev's Five-Card 
		// Poker Hand Evaluator" to determine the relative strength of two five-card hands.  
		// Reference: http://www.suffecool.net/poker/evaluator.html
		
		hand = new int[8];
		int currentCard;
		int rank;
		int handRank = 9999;
		int holdrank = 9999;
		int suit     = 0;
		int numCards = 0;

        final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
         
        if (key != 0) {

            for (cardIndex = 0; cardIndex < 7; cardIndex++) {

            	currentCard = (int)((key >>> (8 * cardIndex)) & 0xFF);
                if (currentCard == 0) break;
                numCards++;
                
                // Cactus Kev Card Representation
                // +--------+--------+--------+--------+
                // |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
                // +--------+--------+--------+--------+
                // p    = prime number of rank (deuce = 2, trey = 3, four = 5, five = 7,..., ace = 41)
                // r    = rank of card         (deuce = 0, trey = 1, four = 2, five = 3,..., ace = 12)
                // cdhs = suit of card
                // b    = bit turned on depending on rank of card
                
                // extract suit and rank from 8-bit packed representation
                rank = (currentCard >>> 4) - 1;	
                suit = currentCard & 0xF; 

                // change card representation to Cactus Kev Representation
                hand[cardIndex] = primes[rank] | (rank << 8) | (1 << (suit + 11)) | (1 << (16 + rank));
            }
            
            switch (numCards) { 
                case 5 :  
                	
                	holdrank = eval_5hand(hand[0],hand[1],hand[2],hand[3],hand[4]);
                	break;

                case 6 :
                    
                	// Cactus Kev's Evaluator ranks hands from 1 (Royal Flush) to 7462 (Seven High Card)
                    holdrank = eval_5hand(                   hand[0],hand[1],hand[2],hand[3],hand[4]);
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[3],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[3],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[2],hand[3],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[2],hand[3],hand[4],hand[5]));
                    break;
                    
                case 7 :
                	
                    holdrank = eval_5hand(                   hand[0],hand[1],hand[2],hand[3],hand[4]);
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[3],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[3],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[4],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[2],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[3],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[3],hand[4],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[3],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[1],hand[4],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[2],hand[3],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[2],hand[3],hand[4],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[2],hand[3],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[2],hand[4],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[0],hand[3],hand[4],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[2],hand[3],hand[4],hand[5]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[2],hand[3],hand[4],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[2],hand[3],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[2],hand[4],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[1],hand[3],hand[4],hand[5],hand[6]));
                    holdrank = Math.min(holdrank, eval_5hand(hand[2],hand[3],hand[4],hand[5],hand[6]));
                    break;
                    
                default :
                	
                    System.out.println("ERROR: Invalid hand in GetRank method.");
                    break;
                    
            }
            
            // Hand Rank Representation
            // +--------+--------+
            // |hhhheeee|eeeeeeee|
            // +--------+--------+
            // h    = poker hand	    (1 = High Card, 2 = One Pair, 3 = Two Pair,..., 9 = Straight Flush)				
            // e    = equivalency class (Rank of equivalency class relative to base hand)
            
            // +-----------------------------------+----------------------------------+-----------------+
            // 		5-Card Equivalency Classes			7-Card Equivalency Classes
            // +-----------------------------------+----------------------------------+-----------------+
            // 	        1277     							 407		High Card
            //		2860 								1470		One Pair
            //		 858 								 763		Two Pair
            //		 858 								 575		Three of a Kind
            //		  10 								  10		Straight
            //		1277 								1277		Flush
            //		 156 								 156		Full House
            //		 156 								 156		Four of a Kind
            //		  10								  10		Straight Flush
            // +----------+------------------------+----------------------------------+-----------------+
            //    Total:    7462								4824
            // +----------+------------------------+----------------------------------+-----------------+
            
            handRank = 7463 - holdrank;  // Invert ranking metric (1 is now worst hand)
                      														          
            if      (handRank < 1278) handRank = handRank -    0 + 4096 * 1; // High Card 
            else if (handRank < 4138) handRank = handRank - 1277 + 4096 * 2; // One Pair  
            else if (handRank < 4996) handRank = handRank - 4137 + 4096 * 3; // Two Pair   
            else if (handRank < 5854) handRank = handRank - 4995 + 4096 * 4; // Three of a Kind   
            else if (handRank < 5864) handRank = handRank - 5853 + 4096 * 5; // Straight     
            else if (handRank < 7141) handRank = handRank - 5863 + 4096 * 6; // Flush   
            else if (handRank < 7297) handRank = handRank - 7140 + 4096 * 7; // Full House    
            else if (handRank < 7453) handRank = handRank - 7296 + 4096 * 8; // Four of a Kind    
            else                      handRank = handRank - 7452 + 4096 * 9; // Straight Flush     
            
        }
        return handRank;
        
	} // END getHandRank method
	
	static int[] offsets = new int[] {0, 1277, 4137, 4995, 5853, 5863, 7140, 7296, 7452};
	
	private static int getIndex(int key) {

		// use binary search to find key
		int low = -1;
		int high = 4888;
		int pivot;

		while (high - low > 1) {
			pivot = (low + high) >>> 1;
			if (Products.table[pivot] > key) {
				high = pivot;
			}
			else if (Products.table[pivot] < key) {
				low = pivot;
			}
			else {
				return pivot;	
			}
		}
		return -1;
		
	} // END getIndex method

	
	
	private static int eval_5hand(int c1, int c2, int c3, int c4, int c5) {
		int   q = (c1 | c2 | c3 | c4 | c5) >> 16;
		short s;
		
		// check for Flushes and Straight Flushes
		if ((c1 & c2 & c3 & c4 & c5 & 0xF000) != 0) return Flushes.table[q];
		
		// check for Straights and High Card hands
		if ((s = Unique.table[q]) != 0) return s;    
		
		q = (c1 & 0xFF) * (c2 & 0xFF) * (c3 & 0xFF) * (c4 & 0xFF) * (c5 & 0xFF);
		q = getIndex(q);
		 
		return Values.table[q];
		
	} // END eval_5hand method
	
	
	
	private static void generateTables() {

	        int  card;
	        int  handRank;
	        int  keyIndex;
	        long key;    
	        
	        if (verbose) {
	        	System.out.print("\nGenerating and sorting keys...");
	        	startTimer = System.currentTimeMillis();
	        }
	        
	        for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) {
            if (keyIndex % (size / 100) == 0) System.out.println(keyIndex / (size / 100) + "% generated.");

              for (card = 1; card < 53; card++) {	      			// add a card to each previously calculated key
	            	key = makeKey(keys[keyIndex], card);  			// create the new key
	            	
	                if (numCards < 7) insertKey(key);				// insert the new key into the key lookup table			
	            }           
	        }
	        
	        if (verbose) {
	        	stopTimer = System.currentTimeMillis();
	        	System.out.printf("done.\n\n%35s %d\n", "Number of Keys Generated:", (keyIndex + 1));
	        	System.out.printf("%35s %f seconds\n\n", "Time Required:", ((stopTimer - startTimer) / 1000.0));
		        System.out.print("Generating hand ranks...");
		        startTimer = System.currentTimeMillis();
	        }
	        
	        for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) { 
            if (keyIndex % (size / 100) == 0) System.out.println(keyIndex / (size / 100) + "% generated.");
	            
	            for (card = 1; card < 53; card++) {
	            	key = makeKey(keys[keyIndex], card);
	                
	                if (numCards < 7) {
	                	handRank = insertKey(key) * 53 + 53;  		// if number of cards is < 7 insert key
	                }
	                else {
	                	handRank = getHandRank(key);   				// if number of cards is 7 insert hand rank
	                }

	                maxHandRankIndex = keyIndex * 53 + card + 53;	// calculate hand rank insertion index
	                handRanks[maxHandRankIndex] = handRank;			// populate hand rank lookup table with appropriate value
	            }
	            
	            if (numCards == 6 || numCards == 7) {
	            	// insert the hand rank into the hand rank lookup table
	            	handRanks[keyIndex * 53 + 53] = getHandRank(keys[keyIndex]);
	            }
	        }
	        
	        if (verbose) {
	        	stopTimer = System.currentTimeMillis();
	        	System.out.printf("done.\n\n%35s %f seconds\n\n", "Time Required:", ((stopTimer - startTimer) / 1000.0));
	        }
	        
	} // END generateTables method
	
	



	private static void saveTables() throws FileNotFoundException, IOException {
		ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(HAND_RANKS_FILE));
		s.writeObject(handRanks);
	}

	/**
	 * 
	 * @param sevenCardHand
	 * @return The rank of the hand. Note a faster implementation might set each of the 
	 * cards individually from left to right, and only change the rightmost cards as required.
	 */
	public static int getRank(Hand sevenCardHand) {
		//System.out.println("Getting rank..");
		Card[] cards = sevenCardHand.toCards();
		int rank = 53;
		for (int i = 0; i < cards.length; i++) {
			int c = cards[i].ordinal() + 1;
			rank = handRanks[c + rank];
		}
		//System.out.println("Got rank");
		int type = (rank >>> 12) - 1; 
		rank = rank & 0xFFF;
		
		return offsets[type] + rank - 1;
	}
	
	public static int getRank(Card[] cards) {
		//System.out.println("Getting rank..");
		//Card[] cards = sevenCardHand.toCards();
		int rank = 53;
		for (int i = 0; i < cards.length; i++) {
			int c = cards[i].ordinal() + 1;
			rank = handRanks[c + rank];
		}
		//System.out.println("Got rank");
		int type = (rank >>> 12) - 1;
		rank = rank & 0xFFF;

		return offsets[type] + rank - 1;
	}
	
	
	

} // END class Evaluator







