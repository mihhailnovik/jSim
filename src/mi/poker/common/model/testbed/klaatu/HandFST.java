package mi.poker.common.model.testbed.klaatu;

import java.io.*;
import java.util.*;

// We use Steve Brecher's HandEval during FST generation, although any correct hand evaluator would work as well

/**
 * FST (Finite State Transducer) generator helper class for the {@code FSTEval} poker hand evaluation methods.
 * <p>
 * This class generates the finite state transducer arrays used by {@code FSTEval} to evaluate poker hands.
 * The evaulation arrays and associated utility map data are serialized and stored in files.  When a request 
 * is made for the arrays, an attempt is made to read them from the serialized files.  If the files
 * are not found, the arrays are regenerated.
 * <p>
 * Input to the FST are the poker cards that form the hand.  In order to keep the FST at a manageable
 * size, only the ranks of the cards are considered.  The initial state of the FST represents a zero-card hand.
 * Each dealt card causes a transition to another valid FST state (there are no invalid inputs in a poker
 * hand).  Because card order does not affect the value of the hand, states with different card permutations 
 * are coalesced. States with identical transistions for every input card (e.g. the states for the six-card 
 * hands [2 2 2 3 5 5] and [2 2 2 4 5 5] in a seven-card game) are also coalesced.  The final FST states are the 
 * equivalence class numbers of the best 5-card high poker hand that can be made from the input cards
 * (5, 6, and 7-card games are supported).
 * <p>
 * The FST states are compactly written as {@code char} arrays (Java's {@code unsigned short} type).  For an n-card
 * game, Each array entry in the first through n-1 arrays, when added to the next card rank value, gives the 
 * index of the entry into the next-level array.  The entries in the last array are the 
 * equivalence class values ([0..4823] for a 7-card game, [0..6074] for a 6-card game and [0..7461] for a
 * 5-card game).
 * <p>
 * A separate flush array is also generated.  For each encoded {@code int} bit vector array index of 
 * the 5-n suited cards of the hand, the array entry at that index is the equivalence class value of the
 * flush or straight flush hand.
 * <p>
 * As an optimization, a separate array is written that combines the rank values of the first three
 * cards.  That is, for each index value {@code ((((rankOf(card1)) << 8) | (rankOf(card2) << 4)) | rankOf(card3))},
 * the array entry at that index contains a value that may be used to index into the fourth-card array.
 * <p>
 * For seven card hands, the five evaluation arrays (four single card arrays and the first three-card array)
 * together with the flush array total 641350 bytes in memory.  
 * <p>
 * For six card hands, the five evaluation arrays (four single card arrays and the first three-card array)
 * together with the flush array total 242614 bytes in memory.  
 * <p>
 * For five card hands, the five evaluation arrays (four single card arrays and the first three-card array)
 * together with the flush array total 82064 bytes in memory.  
 * <p>
 * The generation routines use the {@code com.stevebrecher.poker.HandEval} evaulation routines 
 * to generate the equivalence class data, although any correct hand evaluator would suffice.
 * An array and HashMap are also generated that convert equivalence class numbers to 
 * {@code HandEval} return values and vice versa.
 * <p>
 * After being generated, the arrays and map data are serialized and written to the files HandFST&ltn&gt.ser
 * which are by default located in the current directory. The directory of the HandFST&ltn&gt.ser files
 * may be changed by invoking the method {@code setDirectory} prior to being used.
 * <p>
 * @author Klaatu
 */

public class HandFST {

    private static final int RANK_SIZE = 13;
    private static final int SUIT_COUNT = 4;
    private static final int WIDE_RANK_SIZE = 16; // next power of 2 above RANK_SIZE

    /**
     * Controls whether "wide arrays" are used when the lookup value overflows Character.MAX_VALUE. 
     * When this overflow occurs the lookup value cannot be pre-multiplied by the rank count (13). 
     * If OPT_WIDE_RANK_SIZE is set to the next power of two (16), these array sizes 
     * are rounded up so that the eval routines can use shift instructions instead of multiplications.
     * In the 7-card FST, this adds an additional ~125K of memory to the arrays but allows the eval7 routine 
     * to replace two multiplication instructions with shift instructions.  
     * <p>
     * Using wide arrays speeds up hand enumerations by around 5%, but slows down random evaluations 
     * due to the extra memory required.  It's a close call, but they're turned off by default for now.
     * If you would like to turn them on, set the value of OPT_WIDE_RANK_SIZE to be WIDE_RANK_SIZE,
     * and recompile {@code FSTEval.java} and {@code HandFST.java}.
     * <p>
     * We continue to use a "wide" index for the (small) card123 jump start array, however,
     * since the additional memory cost is negligible.
     */
    public static final int OPT_WIDE_RANK_SIZE = /* WIDE_RANK_SIZE */ RANK_SIZE;

    private static String _cardValues = "23456789TJQKA";    // for debugging
    private static final boolean DEBUG = false;

    // the default directory for the serialized arrays
    private static String _directory = "";

    private int _handLength;
    private int _numStates = 0;
    private String _filename;

    // states of the transducer
    private Vector<HandState> _allStatesVec  = new Vector<HandState>();
    private HandState[] _allStates;
    private HashMap<HandState,HandState> _stateSet = new HashMap<HandState,HandState>();

    private int[] _equivalentStates;    // set by minimize(), used to coalesce equivalent states

    // states, partitioned by "level" (i.e., card count)
    private HandState[][] _levelStates;

    // the exported arrays and map
    private char[][] _levelArrays;
    private char[] _jumpStartArray;
    private char[] _wide6Array;
    private char[] _wide7Array;
    private char[] _flushArray;
    private int[] _toBrecherArray;
    private HashMap<Integer, Integer> _fromBrecherMap;

    // Package-visible constructors
    HandFST(int handLength, String filename) {
    if (handLength < 5)
        throw new UnsupportedOperationException("Too few cards for a poker hand: " + handLength);
    if (handLength > 7)
        throw new UnsupportedOperationException("Unsupported hand size: " + handLength);
    _handLength = handLength;
    _filename = filename;
    }
    HandFST(int handLength) {
    this(handLength, _directory + "HandFST" + handLength + ".ser");
    }
    HandFST(String filename) {
    this(7, filename);
    }
    HandFST() {
    this(7);
    }

    /**
     * Sets the current default directory in which to store serialized FST data. The
     * default directory is used by the {@code HandFST} constructors that do not take 
     * a filename argument.
     * @param dir the current default directory in which to store serialized FST data.
     */
    public static void setDirectory(String dir) {
    if (dir == null)
        dir = "";
    if (!dir.equals("") && !dir.endsWith(File.separator))
        dir = dir + File.separator;
    _directory = dir;
    }

    /**
     * Returns the current default directory in which to store serialized FST data.
     * @return the current default directory in which to store serialized FST data.
     */
    public static String directory() {
    return _directory;
    }

    // Package-visible methods to return the FST evaluation and utility arrays
    char[] evalCard1() { 
    ensureArrays(1);
    return _levelArrays[0];
    }

    char[] evalCard2() { 
    ensureArrays(2);
    return _levelArrays[1];
    }
    char[] evalCard3() { 
    ensureArrays(3);
    return _levelArrays[2];
    }
    char[] evalCard4() { 
    ensureArrays(4);
    return _levelArrays[3];
    }
    char[] evalCard5() { 
    ensureArrays(5);
    return _levelArrays[4];
    }
    char[] evalCard6() { 
    ensureArrays(6);
    if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
        return _wide6Array;
    else
        return _levelArrays[5];
    }
    char[] evalCard7() { 
    ensureArrays(7);
    if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
        return _wide7Array;
    else
        return _levelArrays[6];
    }
    char[] evalFlush() { 
    ensureArrays(0);
    return _flushArray;
    }

    int[] toBrecher() { 
    ensureArrays(0);
    return _toBrecherArray;
    }

    HashMap fromBrecher() {
    ensureArrays(0);
    return _fromBrecherMap;
    }

    char[] evalCards123() { 
    ensureArrays(3);
    return _jumpStartArray;
    }

    // Have the exported arrays been initialized
    private boolean haveArrays() {
    return
        _jumpStartArray != null &&
        _levelArrays != null && 
        _flushArray != null && 
        _toBrecherArray != null && 
        _toBrecherArray != null && 
        _fromBrecherMap != null;
    }

    // Ensure that the exported arrays been initialized, else try to rebuild them, 
    // else throw an exception
    private void ensureArrays(int level) {
    if (level > _handLength)
        throw new IllegalStateException("Illegal to request card " + level + " arrays from an " + _handLength + " FST");
    try {
        if (!haveArrays())
        readArrays();
    } catch (FileNotFoundException e) {
        System.out.println("FST array file " + _filename + " not found, rebuilding...");
    } catch (IOException e) {
        System.out.println("IO Exception + e + reading FST array file " + _filename + ", rebuilding...");
    } catch (ClassNotFoundException e) {
        // not really possible
    } catch (IllegalStateException e) {
        System.out.println("FST array file " + _filename + " built with different OPT_WIDE_RANK_SIZE, rebuilding...");
    }
    try {
        if (!haveArrays())
        rebuild(true);
    } catch (Exception e) {
        System.out.println("Unexpected exception " + e);
        e.printStackTrace();
    }
    if (!haveArrays())
        throw new IllegalStateException("Unable to find or rebuild FST arrays for " + _handLength + "-card hands");
    }


    /*
     * This class represents a state in the FST that occurs after the array of _cards 
     * (actually just card ranks) has been dealt.
     */
    private abstract class HandState implements Serializable {
    int[] _cards;       // ranks only 
    int _index;     // into _allStates array
    char _levelIndex;   // into _levelArrays[n]

    abstract boolean isFinal();
    abstract char value();  // the generated array value

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < _cards.length; i++) {
        int card = _cards[i];
        sb.append(_cardValues.charAt(card));
        if (i != _cards.length - 1)
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
    boolean legalTransistion(int card) {
        int rankCount = 0;
        for (int i = 0; i < _cards.length; i++) {
        if (_cards[i] == card) {
            rankCount++;
        }
        }
        return rankCount != SUIT_COUNT;
    }
    void init(HandState prev, int card) {
        int prevlen = prev._cards.length;
        _cards = new int[prevlen+1];
        System.arraycopy(prev._cards, 0, _cards, 0, prevlen);
        _cards[prevlen] = card;
        Arrays.sort(_cards);
    }
    }

    /*
     * This class represents a final FST state for a made hand.  Its array 
     * value is the hand equivalence class.
     */
    private class FinalHandState extends HandState {
    int _value = -1;
    char _eqvClass = (char)-1;

    FinalHandState(int[] cards) {
        _cards = cards.clone();
    }
    private FinalHandState(HandState prev, int card) {
        init(prev, card);
    }
    boolean isFinal() {
        return true;
    }
    char value() {
        return _eqvClass;
    }
    public boolean equals (Object o) {
        if (!(o instanceof FinalHandState))
        return false;
        FinalHandState other = (FinalHandState)o;
        return _value == other._value;
    }
    public int hashCode() {
        return _value;
    }
    }

    /*
     * This class represents a final FST state for a made hand.  Its array 
     * value is the index of the state in the corresponding level array
     * (i.e. the same-number-of-cards state array).
     */
    private class NonFinalHandState extends HandState {
    HandState[] _transistions;

    private NonFinalHandState(int[] cards) {
        _cards = cards.clone();
    }
    private NonFinalHandState(NonFinalHandState prev, int card) {
        init(prev, card);
    }
    private NonFinalHandState() {
        _cards = new int[0];
        _index = _allStatesVec.size();
        _transistions = new HandState[RANK_SIZE];
        _allStatesVec.add(this);
    }
    boolean isFinal() {
        return false;
    }
    char value() {
        return _levelIndex;
    }
    public boolean equals (Object o) {
        if (!(o instanceof NonFinalHandState))
        return false;
        NonFinalHandState other = (NonFinalHandState)o;
        return Arrays.equals(_cards, other._cards);
    }
    public int hashCode() {
        return Arrays.hashCode(_cards);
    }
    }

    // Intern a non final state that occurs after the transisition of card from 
    // the state prev
    private NonFinalHandState internNonFinalState(NonFinalHandState prev, int card) {
    NonFinalHandState testState = new NonFinalHandState(prev, card);
    NonFinalHandState currState = (NonFinalHandState)_stateSet.get(testState);
    if (currState == null) {
        testState._transistions = new HandState[RANK_SIZE];
        testState._index = _allStatesVec.size();
        _allStatesVec.add(testState);
        _stateSet.put(testState, testState);
        currState = testState;
    }
    prev._transistions[card] = currState;
    return currState;
    }

    // Intern a final state that occurs after the transisition of card from 
    // the state prev
    private FinalHandState internFinalState(NonFinalHandState prev, int card) {
    FinalHandState testState = new FinalHandState(prev, card);
    testState._value = makeHand(testState._cards); // after cards are sorted
    FinalHandState currState = internFinalState(testState);
    prev._transistions[card] = currState;
    return currState;
    }

    // Given an array of card ranks, return an int representing the evaluation
    // of that hand, assuming no flushes occur. We use Steve Brecher's HandEval, 
    // although any correct hand evaluator would do.
    // Requires that ranks are sorted (in order to avoid spurious flushes)
    private static int makeHand(int[] ranks) {
    long bval = bval(ranks);
    return ranks.length == 7 ? 
        HandEval.hand7Eval(bval) : 
        ranks.length == 6 ? 
        HandEval.hand6Eval(bval) : 
        HandEval.hand5Eval(bval);
    }

    // Return a HandEval-encoded int for the specified card ranks. Choose
    // suits in a way to avoid flushes.
    private static long bval(int[] ranks) {
    int suit = 0;   // avoid spurious flushes
    long bval = 0L;
    for (int i = 0; i < ranks.length; i++)
        bval |= 0x1L << (++suit & 3)*RANK_SIZE + ranks[i];
    return bval;
    }

    // Intern a final state with the same cards as testState.
    private FinalHandState internFinalState(FinalHandState testState) {
    FinalHandState currState = (FinalHandState)_stateSet.get(testState);
    if (currState == null) {
        testState._index = _allStatesVec.size();
        _allStatesVec.add(testState);
        _stateSet.put(testState, testState);
        currState = testState;
    }
    return currState;
    }

    // Intern a final flush state with the given cards (card ranks)
    private FinalHandState makeFlush(int[] cards) {
    FinalHandState testState = new FinalHandState(cards);
    testState._value = makeFlushHand(testState._cards);
    return internFinalState(testState);
    }

    // Given an array of card ranks, return an int representing the evaluation
    // of that hand, assuming that all cards are in the same suit. We use Steve 
    // Brecher's HandEval to do the evaluation.
    private static int makeFlushHand(int[] ranks) {
    long bfval = bfval(ranks);
    return ranks.length == 7 ? 
        HandEval.hand7Eval(bfval) : 
        ranks.length == 6 ? 
        HandEval.hand6Eval(bfval) : 
        HandEval.hand5Eval(bfval);
    }

    // Return a HandEval-encoded int for the specified card ranks, assuming that
    // all cards are in the same suit.
    private static long bfval(int[] ranks) {
    long bfval = 0L;
    for (int i = 0; i < ranks.length; i++)
        bfval |= 0x1L << ranks[i];
    return bfval;
    }

    // Rebuild the FST as needed (when the file containing the FST array data is not found)
    private void rebuild(boolean recalculate) {
    if (recalculate)
        calculate();
    minimize();
    buildLevels();
    makeEqvClasses();
    buildLevelArrays();
    makeFlushArray();
    makeToFrom();
    try {
        writeArrays();
    } catch (IOException e) {
        System.out.println("Unable to write FST to " + _filename + ": " + e + ", will rebuild on next use");
    } catch (ClassNotFoundException e) {
        // not really possible
    }
    }
    
    // Calculate the states of the FST
    private void calculate() {
    long start, stop;
    start = System.currentTimeMillis();
    if (_handLength == 7)
        System.out.println("Calculating 7-card FST (this is done one time only and will take a minute or so) ...");
    else 
        System.out.print("Calculating " + _handLength + "-card FST...");
    makeRankStates();
    makeFlushStates();
    _allStates = new HandState[_allStatesVec.size()];
    _allStatesVec.toArray(_allStates);
    _allStatesVec = null;   // help GC
    stop = System.currentTimeMillis();
    if (_handLength == 7)
        System.out.println("7-card FST calculated in " + (stop - start) + " ms");
    else 
        System.out.println("done");
    }

    // Make the states of the FST for all the possible card rank values of the hand
    private void makeRankStates() {
    NonFinalHandState initialState = new NonFinalHandState();
    for (int cn0 = 0; cn0 < RANK_SIZE; cn0++) {
        NonFinalHandState state0 = internNonFinalState(initialState, cn0);
        if (_handLength == 7 && cn0 > 0 && ((cn0*10)/RANK_SIZE > ((cn0-1)*10)/RANK_SIZE))
        System.out.print((cn0*10/RANK_SIZE)*10 + "% ");
        for (int cn1 = 0; cn1 < RANK_SIZE; cn1++) {
        NonFinalHandState state1 = internNonFinalState(state0, cn1);
        for (int cn2 = 0; cn2 < RANK_SIZE; cn2++) {
            NonFinalHandState state2 = internNonFinalState(state1, cn2);
            for (int cn3 = 0; cn3 < RANK_SIZE; cn3++) {
            NonFinalHandState state3 = internNonFinalState(state2, cn3);
            for (int cn4 = 0; cn4 < RANK_SIZE; cn4++) {
                if (state3.legalTransistion(cn4)) {
                if (_handLength == 5) {
                    internFinalState(state3, cn4);
                } else {
                    NonFinalHandState state4 = internNonFinalState(state3, cn4);
                    for (int cn5 = 0; cn5 < RANK_SIZE; cn5++) {
                    if (state4.legalTransistion(cn5)) {
                        if (_handLength == 6) {
                        internFinalState(state4, cn5);
                        } else {
                        NonFinalHandState state5 = internNonFinalState(state4, cn5);
                        for (int cn6 = 0; cn6 < RANK_SIZE; cn6++) {
                            if (state5.legalTransistion(cn6)) {
                            internFinalState(state5, cn6);
                            }
                        }
                        }
                    }
                    }
                }
                }
            }
            }
        }
        }
    }
    }
    
    // Make the final flush states.  These aren't part of the FST (there are no transistions 
    // to them) but we add them to the final state array anyway for convenience
    private void makeFlushStates() {
    for (int cn0 = 0; cn0 < RANK_SIZE; cn0++) {
        for (int cn1 = cn0 + 1; cn1 < RANK_SIZE; cn1++) {
        for (int cn2 = cn1 + 1; cn2 < RANK_SIZE; cn2++) {
            for (int cn3 = cn2 + 1; cn3 < RANK_SIZE; cn3++) {
            for (int cn4 = cn3 + 1; cn4 < RANK_SIZE; cn4++) {
                int[] cards5 = {cn0, cn1, cn2, cn3, cn4};
                makeFlush(cards5);
                if (_handLength > 5) {
                for (int cn5 = cn4 + 1; cn5 < RANK_SIZE; cn5++) {
                    int[] cards6 = {cn0, cn1, cn2, cn3, cn4, cn5};
                    makeFlush(cards6);
                    if (_handLength > 6) {
                    for (int cn6 = cn4 + 1; cn6 < RANK_SIZE; cn6++) {
                        int[] cards7 = {cn0, cn1, cn2, cn3, cn4, cn5, cn6};
                        makeFlush(cards7);
                    }
                    }
                }
                }
            }
            }
        }
        }
    }
    }

    // Minimize the FST by coalescing states with identical transistions.
    private void minimize() {
    long start, stop;
    int numStates = _allStates.length;
    if (DEBUG) {
        start = System.currentTimeMillis();
        System.out.println("Minimizing FST ...");
    }
    _equivalentStates = new int[_allStates.length];
    Arrays.fill(_equivalentStates, -1);
    ArrayList<LinkedList<HandState>> partitions = new ArrayList<LinkedList<HandState>>(numStates);
    for (int i = 0; i < numStates; i++)
        partitions.add(i, new LinkedList<HandState>());
    int deletedCount = 0;
    int partitionCount = 1;
    int[] partIndexes = new int[numStates + 1];
    for (int i = 0; i < numStates; i++) {
        // Place each final state in its own partition, since we want more than a yes/no answer 
        partIndexes[i] = (_allStates[i].isFinal() ? partitionCount++ : 0);
        partitions.get(partIndexes[i]).addFirst(_allStates[i]);
    }
    int[] nextPartIndexes = new int[numStates + 1];
    Arrays.fill(nextPartIndexes, -1);
    Stack<Integer> reverse = new Stack<Integer>();
    int[] nextIndexes = new int[partitions.get(0).size()];
    boolean didSomething = true;
    while (didSomething) {
        didSomething = false;
        for (int pn = 0; pn < partitionCount; pn++) {
        if (partitions.get(pn).size() > 1) {
            for (int tn = 0; tn < RANK_SIZE; tn++) {
            if (partitions.get(pn).size() > 1) {
                int bin = 0;
                for (Iterator it = partitions.get(pn).iterator(); it.hasNext();) {
                HandState state = (HandState)it.next();
                int nxtIdx = numStates;
                if (!state.isFinal()) {
                    NonFinalHandState nfhs = (NonFinalHandState)state;
                    HandState[] trans = nfhs._transistions;
                    if (trans != null) {
                    HandState nhs = trans[tn];
                    if (nhs != null) 
                        nxtIdx = nhs._index;
                    }
                }
                int iNext = partIndexes[nxtIdx];
                if (nextPartIndexes[iNext] == -1) {
                    reverse.push(iNext);
                    nextPartIndexes[iNext] = bin;
                    nextIndexes[bin] = (bin == 0 ? pn : partitionCount++);
                    bin++;
                }
                if (nextPartIndexes[iNext] > 0) {
                    it.remove();
                    int npn = nextIndexes[nextPartIndexes[iNext]];
                    partitions.get(npn).addFirst(state);
                    didSomething = true;
                }
                }
                while (--bin >= 0) {
                if (bin > 0) {
                    for (Iterator it = partitions.get(nextIndexes[bin]).iterator(); 
                     it.hasNext();)
                    partIndexes[((HandState)it.next())._index] = nextIndexes[bin];
                }
                nextPartIndexes[((Integer)reverse.pop()).intValue()] = -1;
                }
            }
            }
        }
        }
    }
    // Coalesce states by setting an index in _equivalentStates
    for (int bn = 0; bn < partitionCount; bn++) {
        if (partitions.get(bn).size() == 0)
        continue;
        Iterator it = partitions.get(bn).iterator();
        int eqvnum = ((HandState)it.next())._index;
        _equivalentStates[eqvnum] = eqvnum;
        while (it.hasNext()) {
        int stateNum = ((HandState)it.next())._index;
        if (stateNum == numStates)
            continue;
        if (DEBUG)
            System.out.println("Coalescing state " + _allStates[stateNum] + 
                       " with " + _allStates[eqvnum]);
        _equivalentStates[stateNum] = eqvnum;
        deletedCount++;
        }
    }
    // Replace all transistion indexes with equivalents
    for (int i = 0; i < _allStates.length; i++) {
        HandState hs = _allStates[i];
        if (!hs.isFinal()) {
        NonFinalHandState nfhs = (NonFinalHandState)hs;
        if (nfhs._transistions != null) {
            for (int j = 0; j < nfhs._transistions.length; j++) {
            HandState trans = nfhs._transistions[j];
            if (trans != null) {
                int txidx = trans._index;
                if (_equivalentStates != null &&
                _equivalentStates[txidx] != -1 &&
                _equivalentStates[txidx] != txidx)
                // This state has been deleted, replace with the equiv state
                nfhs._transistions[j] = _allStates[_equivalentStates[txidx]];
            }
            }
        }
        }
    }
    if (DEBUG) {
        stop = System.currentTimeMillis();
        System.out.println("Removed " + deletedCount + " states");
        System.out.println("FST minimized in " + (stop - start) + " ms");
    }
    }

    // Split the states into separate arrays, one for each number of cards.  This is
    // possible because we never coalesce states with different card numbers.  The FST is
    // ultimately stored as a set of arrays, one per level, to make array index sizes
    // as small as posssible
    private void buildLevels() {
    ArrayList<Vector<HandState>> levelStatesList = new ArrayList<Vector<HandState>>(_handLength+1);
    for (int i = 0; i < _handLength+1; i++) {
       levelStatesList.add(i, new Vector<HandState>());
    }
    
    for (int i = 0; i < _allStates.length; i++) {
        HandState hs = _allStates[i];
        if (hs._cards != null && _equivalentStates[hs._index] == hs._index) {
        //int level = hs._transistions == null ? _handLength : hs._cards.length;
        int level = hs.isFinal() ? _handLength : hs._cards.length;
        hs._levelIndex = (char)levelStatesList.get(level).size();
        levelStatesList.get(level).add(hs);
        }
    }

    _levelStates = new HandState[_handLength+1][];
    for (int i = 0; i < _handLength+1; i++) {
        _levelStates[i] = new HandState[levelStatesList.get(i).size()];
        levelStatesList.get(i).toArray(_levelStates[i]);
        if (DEBUG)
        System.out.println("level " + i + " length = " + _levelStates[i].length);
    }
    }

    // Final state comparison class used by makeEqvClasses
    private class StateComp<T> implements Comparator<T> {
    public int compare(Object o1, Object o2) {
        FinalHandState hs1 = (FinalHandState)o1;
        FinalHandState hs2 = (FinalHandState)o2;
        return hs1._value < hs2._value ? -1 : 1;
    }
    }

    // Order all of the final states.  Once ordered, the index into the sorted array
    // is the eqivalence class of the hand (which is the result of the eval methods).
    private void makeEqvClasses() {
    HandState[] finalStates = _levelStates[_handLength];
    if (DEBUG)
        System.out.println("Final state len = " + finalStates.length);
    Arrays.sort(finalStates, new StateComp<HandState>());
    for (int i = 0; i < finalStates.length; i++) {
        ((FinalHandState)finalStates[i])._eqvClass = (char)i;
    }
    }


    // For each level, create the exported form of the array.  We try to keep the 
    // arrays as small as possible by using the smallest array type in which all entries will
    // fit.  If OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE, we also build wider arrays to
    // avoid eval-time multiplications.
    private void buildLevelArrays() {
    _levelArrays = new char[_handLength][];
    for (int i = _handLength -1; i >= 0; i--) {
        _levelArrays[i] = buildLevelArray(i);
    }
    _jumpStartArray = buildJumpStartArray();
    if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE) {
        if (_handLength > 5) {
        _wide6Array = buildWideArray(6);
        if (_handLength > 6) {
            _wide7Array = buildWideArray(7);
        }
        }
    }
    }

    // Build the arrays for a single level.  Non-final level arrays contain indexes into 
    // the next level arrays, pre-multiplied by RANK_SIZE if the index will fit in a char.
    // The final level array contains the equivalence class values for the hand.
    private char[]  buildLevelArray(int n) {
    HandState[] levelStates = _levelStates[n];
    int entryCount = levelStates.length;
    boolean useEqvClass = n == _levelStates.length - 2;
    boolean preMultiply = !useEqvClass && _levelStates[n+1].length * RANK_SIZE <= Character.MAX_VALUE;
    char[] arr = new char[entryCount*RANK_SIZE];
    for (int i = 0; i < levelStates.length; i++) {
        NonFinalHandState hs = (NonFinalHandState)levelStates[i];
        if (hs._cards.length == n) {
        HandState[] trans = hs._transistions;
        int tlen = trans.length;
        for (int j = 0; j < tlen; j++) {
            HandState ns = trans[j];
            if (ns != null) {
            char nsval = ns.value();
            if (preMultiply)
                nsval *= RANK_SIZE;
            arr[i*tlen+j] = nsval;
            }
        }
        }
    }
    return arr;
    }

    // As an optimization, we build a single array for cards one through three.
    // There isn't much fanout yet, so this doesn't waste much space, and it
    // saves eval a couple of memory indirections.
    private char[]  buildJumpStartArray() {
    char[] arr = new char[WIDE_RANK_SIZE*WIDE_RANK_SIZE*RANK_SIZE];
    char[] fst1 = _levelArrays[0];
    char[] fst2 = _levelArrays[1];
    char[] fst3 = _levelArrays[2];
    for (int i = 0; i < RANK_SIZE; i++) { // card1
        for (int j = 0; j < RANK_SIZE; j++) { // card2
        for (int k = 0; k < RANK_SIZE; k++) { // card3
            arr[(((i<<4)+j)<<4)+k] = fst3[fst2[fst1[i]+j]+k];
        }
        }
    }
    if (DEBUG)
        System.out.println("jump start len = " + arr.length);
    return arr;
    }

    // Optionally build wider arrays to avoid eval-time multiplications.
    private char[] buildWideArray(int level) {
    char[] curr = _levelArrays[level-1];
    int rows = curr.length / RANK_SIZE;
    char[] arr = new char[WIDE_RANK_SIZE*rows];
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < RANK_SIZE; j++) {
        arr[i*WIDE_RANK_SIZE+j] = (char)curr[i*RANK_SIZE+j];
        }
    }
    if (DEBUG)
        System.out.println("wide array " + level + " len = " + arr.length);
    return arr;
    }

    // Construct a separate array that holds all of the possible flush and straight flush
    // hand equivalence class values.  The index into the array is a bit vector that
    // contains all of the 5-n cards of the flush suit.
    private void makeFlushArray() {
    _flushArray = new char[0x1fc1];
    for (int cn0 = 0; cn0 < RANK_SIZE; cn0++) {
        for (int cn1 = cn0 + 1; cn1 < RANK_SIZE; cn1++) {
        for (int cn2 = cn1 + 1; cn2 < RANK_SIZE; cn2++) {
            for (int cn3 = cn2 + 1; cn3 < RANK_SIZE; cn3++) {
            for (int cn4 = cn3 + 1; cn4 < RANK_SIZE; cn4++) {
                int[] cards5 = {cn0, cn1, cn2, cn3, cn4};
                FinalHandState flushState5 = makeFlush(cards5);
                int bfval5 = (int)bfval(cards5);
                _flushArray[bfval5] = flushState5._eqvClass;
                if (_handLength > 5) {
                for (int cn5 = cn4 + 1; cn5 < RANK_SIZE; cn5++) {
                    int[] cards6 = {cn0, cn1, cn2, cn3, cn4, cn5};
                    FinalHandState flushState6 = makeFlush(cards6);
                    int bfval6 = (int)bfval(cards6);
                    _flushArray[bfval6] = flushState6._eqvClass;
                    if (_handLength > 6) {
                    for (int cn6 = cn4 + 1; cn6 < RANK_SIZE; cn6++) {
                        int[] cards7 = {cn0, cn1, cn2, cn3, cn4, cn5, cn6};
                        FinalHandState flushState7 = makeFlush(cards7);
                        int bfval7 = (int)bfval(cards7);
                        _flushArray[bfval7] = flushState7._eqvClass;
                    }
                    }
                }
                }
            }
            }
        }
        }
    }
    }

    // Construct the array and HashMap that map equivalence class numbers to 
    // Steve Brecher HandEval numbers.
    private void makeToFrom() {
    HandState[] finalStates = _levelStates[_handLength];
    _toBrecherArray = new int[finalStates.length];
    _fromBrecherMap = new HashMap<Integer, Integer>();
    for (int i = 0; i < finalStates.length; i++) {
        FinalHandState finalState = (FinalHandState)finalStates[i];
        _toBrecherArray[i] = finalState._value;
        _fromBrecherMap.put(new Integer(finalState._value),
                new Integer(i));
    }
    }

    // Serialize the constructed eval arrays and utility maps and write them into a file
    // so that they don't have to be recalculated each time.
    private void writeArrays() throws IOException, ClassNotFoundException {
    FileOutputStream fos = new FileOutputStream(_filename);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeInt(OPT_WIDE_RANK_SIZE);
    oos.writeObject(_jumpStartArray);
    oos.writeObject(_levelArrays[3]);
    oos.writeObject(_levelArrays[4]);
    if (_handLength > 5) {
        if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
        oos.writeObject(_wide6Array);
        else
        oos.writeObject(_levelArrays[5]);
        if (_handLength > 6) {
        if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
            oos.writeObject(_wide7Array);
        else
            oos.writeObject(_levelArrays[6]);
        }
    }
    oos.writeObject(_flushArray);
    oos.writeObject(_toBrecherArray);
    oos.writeObject(_fromBrecherMap);
    oos.close();
    }

    // Read the serialize the constructed eval arrays and utility maps from a file.
    @SuppressWarnings("unchecked")
    private void readArrays() throws IOException, ClassNotFoundException, IllegalStateException {
    FileInputStream fis = new FileInputStream(_filename);
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
    int owrs = ois.readInt();
    if (owrs != OPT_WIDE_RANK_SIZE)
        throw new IllegalStateException();
    _jumpStartArray = (char[])ois.readObject();
    _levelArrays = new char[_handLength][];
    _levelArrays[3] = (char[])ois.readObject();
    _levelArrays[4] = (char[])ois.readObject();
    if (_handLength > 5) {
        if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
        _wide6Array = (char[])ois.readObject();
        else
        _levelArrays[5] = (char[])ois.readObject();
        if (_handLength > 6) {
        if (OPT_WIDE_RANK_SIZE == WIDE_RANK_SIZE)
            _wide7Array = (char[])ois.readObject();
        else
            _levelArrays[6] = (char[])ois.readObject();
        }
    }
    _flushArray = (char[])ois.readObject();
    _toBrecherArray = (int[])ois.readObject();
    _fromBrecherMap = (HashMap<Integer, Integer>)ois.readObject();
    ois.close();
    }
}
