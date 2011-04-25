package mi.poker.common.model.testbed.klaatu;

import java.util.HashMap;

/**
 * Fast (Finite accepting state transducer) poker hand evaluation methods.
 * <p>
 * Each evaluation method takes 5, 6, or 7 cards represented as ints in the range [0..51] and
 * encoded as described in the {@link #encode(int, int)} method.
 * The return value of each evaluation method is an {@code int} representing the equivalence class of 
 * the hand, ordered such that better hands have higher equivalence class numbers.
 * <p>
 * The evaluation algorithm uses finite state tranducers that are generated from all possible 
 * combinations of the ranks of the cards, using the code in {@code HandFST}.  The output of 
 * each tranducer is the equivalence class of the best 5-card poker hand, chosen from the number of 
 * argument cards, ignoring the suits of the cards.  The minimized tranducers are compactly 
 * stored as {@code char} (Java's unsigned short type) arrays, one array for cards one through three 
 * and one array for each of the remaining cards.
 * <p>
 * Evaluation begins by combining the ranks of the first three cards to form an index to 
 * lookup a value in the first array.  For each remaining card, the lookup value obtained from
 * the last array is added to the rank of the card to obtain an index into the next lookup array.
 * The values in the final array are the equivalence class values for the hand, assuming that the cards 
 * do not form a flush or straight flush.
 * <p>
 * Independently, suit counts of each of the seven cards are tallied.  If the cards do not
 * from a flush or straight flush, then the equivalence class value from the last FST array is returned.
 * Otherwise, the cards of the flush suit are combined to form an index that is used to lookup an 
 * equivalence class value from a separate flush array.
 * <p>
 * The transducer and flush arrays are read from files HandFST&ltn&gt.ser which are by default located 
 * in the current directory. If the files do not exist, they are generated on first use.  The directory 
 * of the HandFST&ltn&gt.ser files may be changed by invoking the method {@code HandFST.setDirectory} 
 * prior to the first reference to the {@code FastEval} class.
 * <p>
 * Return values from the different eval&ltn&gt methods are not comparable.  However, for compatibility,
 * return values may be passed as arguments to the methods brecher&ltn&gt which will convert the
 * return values into "Steve Brecher" {@code com.stevebrecher.poker.HandEval} format.  Brecher-format 
 * values are comparable across different hand lengths and may also be used to determine the type of 
 * hand that the return value represents.
 * <p>
 * All methods are thread-safe.
 * <p>
 * For better performance when running the Sun JDK Java, use the -server option.
 * <p>
 * @author Klaatu
 */

public final class PartialStageFastEval {

    // An arbitrary suit designation
    private static final int CLUBS = 0;
    private static final int DIAMONDS = 1;
    private static final int HEARTS = 2;
    private static final int SPADES = 3;

    private static final int SUIT_COUNT = 4;
    private static final int SUIT_MASK = SUIT_COUNT - 1;
    private static final int LOG2_SUIT_COUNT = 2;

    private static final int FLUSH_COUNT = 5; // cards needed for a flush
    // SUIT_INIT value chosen to push made-flush suit counts into next nibble bits
    private static final int SUIT_INIT = 3;

    private static final int CLUB_FLUSH = (1 << (CLUBS << LOG2_SUIT_COUNT)) * (SUIT_INIT + FLUSH_COUNT);
    private static final int DIAMOND_FLUSH = (1 << (DIAMONDS << LOG2_SUIT_COUNT)) * (SUIT_INIT + FLUSH_COUNT);
    private static final int HEART_FLUSH = (1 << (HEARTS << LOG2_SUIT_COUNT)) * (SUIT_INIT + FLUSH_COUNT);
    private static final int SPADE_FLUSH = (1 << (SPADES << LOG2_SUIT_COUNT)) * (SUIT_INIT + FLUSH_COUNT);

    private static final int FLUSH_MASK = CLUB_FLUSH | DIAMOND_FLUSH | HEART_FLUSH | SPADE_FLUSH;

    private static final int SUIT_INIT_MASK = ((1 << (CLUBS << LOG2_SUIT_COUNT)) * SUIT_INIT) | ((1 << (DIAMONDS << LOG2_SUIT_COUNT)) * SUIT_INIT)
            | ((1 << (HEARTS << LOG2_SUIT_COUNT)) * SUIT_INIT) | ((1 << (SPADES << LOG2_SUIT_COUNT)) * SUIT_INIT);

    private static final int RANK_COUNT = 13; // Number of different card ranks

    // The next power of two above RANK_COUNT -- used when we can't pre-multiply lookup array values 
    // by RANK_COUNT because we'd overflow an unsigned short (or when we choose not to for the first
    // two cards)
    private static final int WIDE_RANK = 16;
    private static final int WIDE_RANK_SHIFT = 4; // log2(WIDE_RANK)
    private static final int RANK_MASK = ~SUIT_MASK;
    private static final int ONE_CARD_WIDE_RANK_SHIFT = WIDE_RANK_SHIFT - LOG2_SUIT_COUNT;

    private int card1, card2, card3, card4, card5, card6;
    private int sval1, sval2, sval3, sval4, sval5, sval6, sval7;
    private int rval123, rval4, rval5, rval6;

    public final void setCard1(int card) {
        card1 = card;
        sval1 = SUIT_INIT_MASK + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
    }

    public final void setCard2(int card) {
        card2 = card;
        sval2 = sval1 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
    }

    public final void setCard3(int card) {
        card3 = card;
        sval3 = sval2 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
        rval123 = fst7123[((card1 & RANK_MASK) << (ONE_CARD_WIDE_RANK_SHIFT + WIDE_RANK_SHIFT)) + ((card2 >> LOG2_SUIT_COUNT) << WIDE_RANK_SHIFT)
                + (card3 >> LOG2_SUIT_COUNT)];
    }

    public final void setCard4(int card) {
        card4 = card;
        sval4 = sval3 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
        rval4 = fst74[rval123 + (card >> LOG2_SUIT_COUNT)];
    }

    public final void setCard5(int card) {
        card5 = card;
        sval5 = sval4 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
        rval5 = fst75[rval4 + (card >> LOG2_SUIT_COUNT)];
    }

    public final void setCard6(int card) {
        card6 = card;
        sval6 = sval5 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
        rval6 = fst76[(rval5 * HandFST.OPT_WIDE_RANK_SIZE) + (card >> LOG2_SUIT_COUNT)] * HandFST.OPT_WIDE_RANK_SIZE;
    }

    public final int setHand7(int card) {
        sval7 = sval6 + (1 << ((card & SUIT_MASK) << LOG2_SUIT_COUNT));
        if ((sval7 & FLUSH_MASK) == 0) {
            return fst77[rval6 + (card >> LOG2_SUIT_COUNT)];
        }
        // Find the cards that form the flush
        int suit = (((sval7 & (CLUB_FLUSH | DIAMOND_FLUSH)) != 0) ? (((sval7 & CLUB_FLUSH) != 0) ? CLUBS : DIAMONDS) : (((sval7 & SPADE_FLUSH) != 0) ? SPADES
                : HEARTS));
        int suitedCards = 0;
        if ((card1 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card1 >> LOG2_SUIT_COUNT));
        if ((card2 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card2 >> LOG2_SUIT_COUNT));
        if ((card3 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card3 >> LOG2_SUIT_COUNT));
        if ((card4 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card4 >> LOG2_SUIT_COUNT));
        if ((card5 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card5 >> LOG2_SUIT_COUNT));
        if ((card6 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card6 >> LOG2_SUIT_COUNT));
        if ((card & SUIT_MASK) == suit)
            suitedCards |= (1 << (card >> LOG2_SUIT_COUNT));
        return flush7[suitedCards];
    }

    /**
     * Returns the suit of an encoded card, a value from [0..3], representing an arbitrary card suit.
     * <p>
     * The {@code suitOf} method returns the value {@code (card & 3)}.
     *
     * @param card an encoded card
     * @return the suit of the card
     */
    public static int suitOf(int card) {
        return card & SUIT_MASK;
    }

    /**
     * Returns the rank of an encoded card, a value from [0..12], representing the values [2..A].
     * <p>
     * The {@code rankOf} method returns the value {@code (card >> 2)}.
     *
     * @param card an encoded card
     * @return the rank of the card
     */
    public static int rankOf(int card) {
        return card >> LOG2_SUIT_COUNT;
    }

    /**
     * Returns an encoded card value suitable for passing as an argument to one of the {@code eval} 
     * methods.
     * The rank argument is an {@code int} from [0..12] representing the cards [2..A].
     * The suit argument is an {@code int} from [0..3], chosen arbitrarily.
     * The return value is an {@code int} from [0..51].
     * <p>
     * The {@code encode} method returns the value {@code ((rank << 2) | suit)}.
     *
     * @param rank the rank of the card, a value from 0 to 12, inclusive
     * @param suit the suit of the card, a value from 0 to 3, inclusive
     * @return an encoded card value suitable for passing as an argument to one of 
     * the {@code eval} methods.
     */
    public static int encode(int rank, int suit) {
        return (rank << LOG2_SUIT_COUNT) | suit;
    }

    /**
     * Returns the equivalence class value of the best five-card high poker hand chosen
     * from the specified seven cards, ordered such that better hands have higher equivalence
     * class numbers.
     * The returned equivalence class value is a number between 0 and 4823, inclusive.
     * Each card is an {@code int} encoded as described in the {@code encode} method.
     * Cards may be specified in any order.
     * <p>
     * The evaluation algorithm uses a finite state transducer that is generated from all possible 
     * combinations of the ranks of the seven cards.  The output of the transducer is a number 
     * representing one of the 3537 possible distinct five-card poker hands chosen from seven cards
     * that are not flushes or straight flushes.  The minimized transducer contains 23890 non-final 
     * states and is stored as five {@code char} arrays, one array for cards one through 
     * three and one array for each of the remaining four cards.
     * <p>
     * Evaluation begins by combining the ranks of the first three cards to form an index to 
     * lookup a value in the first array.  For each remaining card, the lookup value obtained from
     * the last array is added to the rank of the card to obtain an index into the next lookup array.
     * The values in the final array are the equivalence class values for the hand, assuming that 
     * the cards do not form a flush or straight flush.
     * <p>
     * Independently, suit counts of each of the seven cards are tallied.  If the cards do not
     * from a flush or straight flush (this is the case 96.94% of the time for randomly chosen hands), 
     * then the equivalence class value from the last FST array is returned.  Otherwise, the
     * cards of the flush suit are combined to form an index that is used to lookup an equivalence class
     * value from a separate flush array.
     * <p>
     * The five transducer arrays and the flush array together total 641350 bytes in memory.
     * The transducer and flush arrays are read from the file HandFST7.ser which is by default 
     * located in the current directory. If the file does not exist, it is generated on first use.
     * The directory of HandFST7.ser may be changed by invoking the method {@code HandFST.setDirectory}
     * prior to the first reference to the {@code FastEval} class.
     *
     * @param card1 the first encoded card value
     * @param card2 the second encoded card value
     * @param card3 the third encoded card value
     * @param card4 the fourth encoded card value
     * @param card5 the fifth encoded card value
     * @param card6 the sixth encoded card value
     * @param card7 the seventh encoded card value
     * @see #encode(int, int)
     * @return the equivalence class value of the best five-card high poker hand chosen
     *         from the specified seven cards, ordered such that better hands have higher equivalence
     *         class numbers. The returned equivalence class value is a number between 0 and 4823, 
     *         inclusive.
     */
    public static final int eval7(int card1, int card2, int card3, int card4, int card5, int card6, int card7) {

        int sval = SUIT_INIT_MASK + (1 << ((card1 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card2 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card3 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card4 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card5 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card6 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card7 & SUIT_MASK) << LOG2_SUIT_COUNT));

        if ((sval & FLUSH_MASK) == 0) {
            // done with the 96.94% of all hands that aren't flushes or straight flushes
            // The java -client (default) compiler generates less optimal code if all of the array
            // accesses are combined into one statement (which is what java -server prefers, on the
            // other hand).  The following is a compromise.
            int rval = fst7123[((card1 & RANK_MASK) << (ONE_CARD_WIDE_RANK_SHIFT + WIDE_RANK_SHIFT)) + ((card2 >> LOG2_SUIT_COUNT) << WIDE_RANK_SHIFT)
                    + (card3 >> LOG2_SUIT_COUNT)];
            rval = fst77[(fst76[(fst75[fst74[rval + (card4 >> LOG2_SUIT_COUNT)] + (card5 >> LOG2_SUIT_COUNT)] * HandFST.OPT_WIDE_RANK_SIZE)
                    + (card6 >> LOG2_SUIT_COUNT)] * HandFST.OPT_WIDE_RANK_SIZE)
                    + (card7 >> LOG2_SUIT_COUNT)];
            return rval;
        }
        // Find the cards that form the flush
        int suit = (((sval & (CLUB_FLUSH | DIAMOND_FLUSH)) != 0) ? (((sval & CLUB_FLUSH) != 0) ? CLUBS : DIAMONDS) : (((sval & SPADE_FLUSH) != 0) ? SPADES
                : HEARTS));
        int suitedCards = 0;
        if ((card1 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card1 >> LOG2_SUIT_COUNT));
        if ((card2 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card2 >> LOG2_SUIT_COUNT));
        if ((card3 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card3 >> LOG2_SUIT_COUNT));
        if ((card4 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card4 >> LOG2_SUIT_COUNT));
        if ((card5 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card5 >> LOG2_SUIT_COUNT));
        if ((card6 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card6 >> LOG2_SUIT_COUNT));
        if ((card7 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card7 >> LOG2_SUIT_COUNT));
        return flush7[suitedCards];
    }

    /**
     * Returns the equivalence class value of the best five-card high poker hand chosen
     * from the specified six cards, ordered such that better hands have higher equivalence class
     * numbers. The returned equivalence class value is a number between 0 and 6074, inclusive.
     * Each card is an {@code int} encoded as described in the {@code encode} method.
     * Cards may be specified in any order.
     * <p>
     * The evaluation algorithm uses a finite state transducer that is generated from all possible 
     * combinations of the ranks of the six cards.  The output of the transducer is a number
     * representing one of the 4788 possible distinct five-card poker hands chosen from six cards 
     * that are not flushes or straight flushes.  The transducer contains 8554 non-final states 
     * and is stored as four {@code char} arrays, one array for cards one through 
     * three and one array for each of the remaining three cards.
     * <p>
     * Evaluation begins by combining the ranks of the first three cards to form an index to 
     * lookup a value in the first array.  For each remaining card, the lookup value obtained from
     * the last array is added to the rank of the card to obtain an index into the next lookup array.
     * The values in the final array are the equivalence class values for the hand, assuming that the
     * cards do not form a flush or straight flush.
     * <p>
     * Independently, suit counts of each of the six cards are tallied.  If the cards do not
     * from a flush or straight flush (this is the case 98.98% of the time for randomly chosen hands),
     * then the equivalence class value from the last FST array is returned.
     * Otherwise, the cards of the flush suit are combined to form an index that is used to lookup
     * an equivalence class value from a separate flush array.
     * <p>
     * The four transducer arrays and the flush array together total 242614 bytes in memory, 18486 
     * bytes of which are shared with the other {@code eval} methods.  The transducer and flush arrays
     * are read from the file HandFST6.ser which is by default located in the current directory. If
     * the file does not exist, it is generated on first use. The directory of HandFST6.ser may be
     * changed by invoking the method {@code HandFST.setDirectory} prior to the 
     * first reference to the {@code FastEval} class.
     *
     * @param card1 the first encoded card value
     * @param card2 the second encoded card value
     * @param card3 the third encoded card value
     * @param card4 the fourth encoded card value
     * @param card5 the fifth encoded card value
     * @param card6 the sixth encoded card value
     * @see #encode(int, int)
     * @return the equivalence class value of the best five-card high poker hand chosen
     *         from the six cards, ordered such that better hands have higher equivalence class numbers.
     *         The returned equivalence class value is a number between 0 and 6074, inclusive.
     */
    public static final int eval6(int card1, int card2, int card3, int card4, int card5, int card6) {
        // One fewer nested access (than in eval7) doesn't cause nearly the same performance issues
        // for java -client. Go figure.
        int rval = fst66[(fst65[fst64[fst6123[((card1 & RANK_MASK) << (ONE_CARD_WIDE_RANK_SHIFT + WIDE_RANK_SHIFT))
                + ((card2 >> LOG2_SUIT_COUNT) << WIDE_RANK_SHIFT) + (card3 >> LOG2_SUIT_COUNT)]
                + (card4 >> LOG2_SUIT_COUNT)]
                + (card5 >> LOG2_SUIT_COUNT)] * HandFST.OPT_WIDE_RANK_SIZE)
                + (card6 >> LOG2_SUIT_COUNT)];
        int sval = SUIT_INIT_MASK + (1 << ((card1 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card2 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card3 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card4 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card5 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card6 & SUIT_MASK) << LOG2_SUIT_COUNT));

        if ((sval & FLUSH_MASK) == 0)
            // done with all hands that aren't flushes or straight flushes
            return rval;

        // Find the cards that form the flush
        int suit = (((sval & (CLUB_FLUSH | DIAMOND_FLUSH)) != 0) ? (((sval & CLUB_FLUSH) != 0) ? CLUBS : DIAMONDS) : (((sval & SPADE_FLUSH) != 0) ? SPADES
                : HEARTS));
        int suitedCards = 0;
        if ((card1 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card1 >> LOG2_SUIT_COUNT));
        if ((card2 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card2 >> LOG2_SUIT_COUNT));
        if ((card3 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card3 >> LOG2_SUIT_COUNT));
        if ((card4 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card4 >> LOG2_SUIT_COUNT));
        if ((card5 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card5 >> LOG2_SUIT_COUNT));
        if ((card6 & SUIT_MASK) == suit)
            suitedCards |= (1 << (card6 >> LOG2_SUIT_COUNT));
        return flush6[suitedCards];
    }

    /**
     * Returns the equivalence class value of the best five-card high poker hand from the
     * specified five cards, ordered such that better hands have higher equivalence class numbers.
     * The returned equivalence class value is a number between 0 and 7461, inclusive.
     * Each card is an {@code int} encoded as described in the {@code encode} method.
     * Cards may be specified in any order.
     * <p>
     * The evaluation algorithm uses a finite state transducer that is generated from all possible 
     * combinations of the ranks of the five cards.  The output of the transducer is a number 
     * representing one of the 6175 possible distinct five-card poker hands that are not flushes 
     * or straight flushes. The transducer contains 2379 non-final states and is stored as three
     * {@code char} arrays, one array for cards one through three and one array for each of the 
     * remaining two cards.
     * <p>
     * Evaluation begins by combining the ranks of the first three cards to form an index to 
     * lookup a value in the first array.  For the remaining two cards, the lookup value obtained from
     * the last array is added to the rank of the card to obtain an index into the next lookup array.
     * The values in the final array are the equivalence class values for the hand, assuming that
     * the cards do not form a flush or straight flush.
     * <p>
     * Independently, suit counts of each of the five cards are tallied.  If the cards do not
     * from a flush or straight flush (this is the case 99.8% of the time for randomly chosen hands), 
     * then the equivalence class value from the last FST array is returned.
     * Otherwise, the cards are combined to form an index that is used to lookup an equivalence 
     * class value from a separate flush array.
     * <p>
     * The three transducer arrays and the flush array together total 82064 bytes in memory, 18486
     * bytes of which are shared with the other {@code eval} methods.  The transducer and flush 
     * arrays are read from the file HandFST5.ser which is by default located in the current directory.
     * If the file does not exist, it is generated on first use. The directory of HandFST5.ser may
     * be changed by invoking the method {@code HandFST.setDirectory} prior to the 
     * first reference to the {@code FastEval} class.
     *
     * @param card1 the first encoded card value
     * @param card2 the second encoded card value
     * @param card3 the third encoded card value
     * @param card4 the fourth encoded card value
     * @param card5 the fifth encoded card value
     * @see #encode(int, int)
     * @return the equivalence class value of the best five-card high poker hand made from the specified
     *         five cards, ordered such that better hands have higher equivalence class numbers.
     *         The returned equivalence class value is a number between 0 and 7461, inclusive.
     */
    public static final int eval5(int card1, int card2, int card3, int card4, int card5) {
        int rval = fst55[fst54[fst5123[((card1 & RANK_MASK) << (ONE_CARD_WIDE_RANK_SHIFT + WIDE_RANK_SHIFT)) + ((card2 >> LOG2_SUIT_COUNT) << WIDE_RANK_SHIFT)
                + (card3 >> LOG2_SUIT_COUNT)]
                + (card4 >> LOG2_SUIT_COUNT)]
                + (card5 >> LOG2_SUIT_COUNT)];
        int sval = SUIT_INIT_MASK + (1 << ((card1 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card2 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card3 & SUIT_MASK) << LOG2_SUIT_COUNT)) + (1 << ((card4 & SUIT_MASK) << LOG2_SUIT_COUNT))
                + (1 << ((card5 & SUIT_MASK) << LOG2_SUIT_COUNT));

        if ((sval & FLUSH_MASK) == 0)
            // done with all hands that aren't flushes or straight flushes
            return rval;

        int suitedCards = (1 << (card1 >> LOG2_SUIT_COUNT)) | (1 << (card2 >> LOG2_SUIT_COUNT)) | (1 << (card3 >> LOG2_SUIT_COUNT))
                | (1 << (card4 >> LOG2_SUIT_COUNT)) | (1 << (card5 >> LOG2_SUIT_COUNT));
        return flush5[suitedCards];
    }

    /**
     * Returns the {@code com.stevebrecher.poker.HandEval} return value that correspdonds to the
     * specified {@code eval5} equivalence class.
     * @param eqvClass an equivalence class value such as returned by {@code eval5}.
     * @return the equivalent {@code com.stevebrecher.poker.HandEval} return value.
     */
    public static final int toBrecher5(int eqvClass) {
        return toBrecher5[eqvClass];
    }

    /**
     * Returns the {@code com.stevebrecher.poker.HandEval} return value that correspdonds to the
     * specified {@code eval6} equivalence class.
     * @param eqvClass an equivalence class value such as returned by {@code eval6}.
     * @return the equivalent {@code com.stevebrecher.poker.HandEval} return value.
     */
    public static final int toBrecher6(int eqvClass) {
        return toBrecher6[eqvClass];
    }

    /**
     * Returns the {@code com.stevebrecher.poker.HandEval} return value that correspdonds to the
     * specified {@code eval7} equivalence class.
     * @param eqvClass an equivalence class value such as returned by {@code eval7}.
     * @return the equivalent {@code com.stevebrecher.poker.HandEval} return value.
     */
    public static final int toBrecher7(int eqvClass) {
        return toBrecher7[eqvClass];
    }

    /**
     * Returns the {@code eval5} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval5}.
     */
    public static final int fromBrecher5(int val) {
        return fromBrecher5(new Integer(val));
    }

    /**
     * Returns the {@code eval5} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval5}.
     */
    public static final int fromBrecher5(Integer val) {
        return ((Integer) fromBrecher5.get(val)).intValue();
    }

    /**
     * Returns the {@code eval6} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval6}.
     */
    public static final int fromBrecher6(int val) {
        return fromBrecher6(new Integer(val));
    }

    /**
     * Returns the {@code eval6} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval6}.
     */
    public static final int fromBrecher6(Integer val) {
        return ((Integer) fromBrecher6.get(val)).intValue();
    }

    /**
     * Returns the {@code eval7} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval7}.
     */
    public static final int fromBrecher7(int val) {
        return fromBrecher7(new Integer(val));
    }

    /**
     * Returns the {@code eval7} equivalence class value that corresponds to the specified
     * {@code com.stevebrecher.poker.HandEval} return value.
     * @param val a {@code com.stevebrecher.poker.HandEval} return value.
     * @return the corresponding equivalence class value that would be returned by {@code eval7}.
     */
    public static final int fromBrecher7(Integer val) {
        return ((Integer) fromBrecher7.get(val)).intValue();
    }

    /*
     *  Initalization
     */

    /* 
     * The FSTs for each 5, 6, or 7-card game are represented as a set of arrays,
     * one per card (with cards 1 and 2 combined for efficiency purposes).
     *
     * MAYBE
     * The arrays are stored in separate inner classes so that only the arrays
     * for the games actually being used are loaded into memory
     *
     * The six seven-card hand FST arrays total approximately 600000 bytes.
     * The five six-card hand arrays total approximately ?00000 bytes.
     * The four five-card hand arrays total approximately ?00000 bytes.
     *
     */

    // These 'char' arrays don't contain actual characters.  The 'char' type
    // is java's version of an unsigned short, which is what we want to use to
    // be space-efficient.

    // Five card hand evalutaion arrays
    private static final char[] fst5123; // cards 1-3
    private static final char[] fst54;
    private static final char[] fst55;
    private static final char[] flush5;

    // Six card hand evalutaion arrays
    private static final char[] fst6123; // cards 1-3
    private static final char[] fst64;
    private static final char[] fst65;
    private static final char[] fst66;
    private static final char[] flush6;

    // Seven card hand evalutaion arrays
    private static final char[] fst7123; // cards 1-3
    private static final char[] fst74;
    private static final char[] fst75;
    private static final char[] fst76;
    private static final char[] fst77;
    private static final char[] flush7;

    /**
     * An array containing {@code int} values that map {@code eval5} equivalence classes to
     * "Steve Brecher" {@code com.stevebrecher.poker.HandEval} format hand values
     */
    private static final int[] toBrecher5;

    /**
     * A HashMap whose {@code Integer} keys map "Steve Brecher" {@code com.stevebrecher.poker.HandEval} 
     * format hand values to  {@code eval5} equivalence classes.
     */
    private static final HashMap fromBrecher5;

    /**
     * An array containing {@code int} values that map {@code eval6} equivalence classes to 
     * "Steve Brecher" {@code com.stevebrecher.poker.HandEval} format hand values
     */
    private static final int[] toBrecher6;

    /**
     * A HashMap whose {@code Integer} keys map "Steve Brecher" {@code com.stevebrecher.poker.HandEval} 
     * format hand values to  {@code eval6} equivalence classes.
     */
    private static final HashMap fromBrecher6;

    /**
     * An array containing {@code int} values that map {@code eval7} equivalence classes to
     * "Steve Brecher" {@code com.stevebrecher.poker.HandEval} format hand values
     */
    private static final int[] toBrecher7;

    /**
     * A HashMap whose {@code Integer} keys map "Steve Brecher" {@code com.stevebrecher.poker.HandEval} 
     * format hand values to  {@code eval7} equivalence classes.
     */
    private static final HashMap fromBrecher7;

    // Read in the arrays.  Normally, these are read from serialized files
    // but they will be rebuilt from scratch if the files are not found
    static {
        HandFST fst7 = new HandFST(7);
        fst7123 = fst7.evalCards123();
        fst74 = fst7.evalCard4();
        fst75 = fst7.evalCard5();
        fst76 = fst7.evalCard6();
        fst77 = fst7.evalCard7();
        flush7 = fst7.evalFlush();
        toBrecher7 = fst7.toBrecher();
        fromBrecher7 = fst7.fromBrecher();

        HandFST fst6 = new HandFST(6);
        // Share first two arrays
        fst6123 = fst7.evalCards123(); // equiv to fst6.evalCards123(), so share
        fst64 = fst7.evalCard4(); // equiv to fst6.evalCard4(), so share
        fst65 = fst6.evalCard5();
        fst66 = fst6.evalCard6();
        flush6 = fst6.evalFlush();
        toBrecher6 = fst6.toBrecher();
        fromBrecher6 = fst6.fromBrecher();

        HandFST fst5 = new HandFST(5);
        // Share first two arrays
        fst5123 = fst7.evalCards123(); // equiv to fst5.evalCards123(), so share
        fst54 = fst7.evalCard4(); // equiv to fst5.evalCard4(), so share
        fst55 = fst5.evalCard5();
        flush5 = fst5.evalFlush();
        toBrecher5 = fst5.toBrecher();
        fromBrecher5 = fst5.fromBrecher();
    }

    /*
     *  Debugging routines (not written to be particularly efficient)
     * 
     *  See Brecher's HandEval.java for a description of the format of the brecher value argument
     */

    static String[] _cardStrings = { "bad1", "bad2", "Deuce", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace" };

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a high-card hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int NO_PAIR = 0; // HandEval.HandCategory.NO_PAIR.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a one-pair hand 
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int PAIR = 1; // HandEval.HandCategory.PAIR.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a two-pair hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int TWO_PAIR = 2; // HandEval.HandCategory.TWO_PAIR.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a trips hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int THREE_OF_A_KIND = 3; // HandEval.HandCategory.THREE_OF_A_KIND.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a straight hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int STRAIGHT = 4; // HandEval.HandCategory.STRAIGHT.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a flush hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int FLUSH = 5; // HandEval.HandCategory.FLUSH.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a full house hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int FULL_HOUSE = 6; // HandEval.HandCategory.FULL_HOUSE.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a quads hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int FOUR_OF_A_KIND = 7; // HandEval.HandCategory.FOUR_OF_A_KIND.ordinal();

    /**
     * A constant (that may be used in a {@code switch} statement) indicating a straight flush hand
     * in the {@code com.stevebrecher.poker.HandEval} return value format.
     */
    public static final int STRAIGHT_FLUSH = 8; // HandEval.HandCategory.STRAIGHT_FLUSH.ordinal();

    /**
     * Given a {@code com.stevebrecher.poker.HandEval} return value, return a string that 
     * describes the made hand.
     * @param brecherValue a {@code com.stevebrecher.poker.HandEval} return value.
     * @return a string that describes the made hand.
     */
    public static String handString(int brecherValue) {
        StringBuffer sb = new StringBuffer();
        int category = brecherValue >> 24;
        int topNibble = (brecherValue >> 20) & 0xf;
        int botNibble = (brecherValue >> 16) & 0xf;
        int kickers = brecherValue & 0xffff;
        switch (category) {
        case PAIR:
            sb.append("a pair of ");
            sb.append(plural(botNibble));
            sb.append(" with a ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 2)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 3)]);
            sb.append(" kicker");
            break;
        case TWO_PAIR:
            sb.append("two pair, ");
            sb.append(plural(topNibble));
            sb.append(" and ");
            sb.append(plural(botNibble));
            sb.append(" with a ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" kicker");
            break;
        case THREE_OF_A_KIND:
            sb.append("three of a kind, ");
            sb.append(plural(botNibble));
            sb.append(" with a ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 2)]);
            sb.append(" kicker");
            break;
        case STRAIGHT:
            sb.append("a straight, ");
            sb.append(_cardStrings[botNibble]);
            sb.append(" high ");
            break;
        case FLUSH:
            sb.append("a flush, ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 2)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 3)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 4)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 5)]);
            break;
        case FULL_HOUSE:
            sb.append("a full house, ");
            sb.append(plural(botNibble));
            sb.append(" full of ");
            sb.append(plural(kicker(kickers, 1)));
            break;
        case FOUR_OF_A_KIND:
            sb.append("four of a kind, ");
            sb.append(plural(botNibble));
            sb.append(" with a ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" kicker");
            break;
        case STRAIGHT_FLUSH:
            sb.append("a straight flush, ");
            sb.append(_cardStrings[botNibble]);
            sb.append(" high ");
        default:
            sb.append("high card ");
            sb.append(_cardStrings[kicker(kickers, 1)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 2)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 3)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 4)]);
            sb.append(" ");
            sb.append(_cardStrings[kicker(kickers, 5)]);
        }
        return sb.toString();
    }

    private static int kicker(int mask, int pos) {
        int found = 0;
        int bit = 0x1000;
        int bnum = 14;
        while (bit != 0) {
            if ((bit & mask) != 0 && ++found == pos)
                return bnum;
            bnum--;
            bit >>= 1;
        }
        return 0;
    }

    private static String plural(int val) {
        return _cardStrings[val] + (val == 6 ? "es" : "s");
    }
}

