package mi.poker.common.model.testbed.klaatu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


/**
 * A set of distinct {@link Card}s.
 * <p>This implementation is a wrapper on {@link ArrayList}<{@link Card}>
 * that allows no duplicates.  A CardSet's iterator will provide elements
 * in FIFO order -- the order in which the elements were added -- if the
 * instance's shuffle method has not been invoked.
 * <p>It also provides a 52-card deck.
 * <p>Methods not otherwise documented forward to {@link ArrayList}<{@link Card}> or perform
 * as specified by the {@link Set} interface.
 * @version 2006Dec10.0
 * @author Steve Brecher
 *
 */
public class CardSet implements Set<Card> {
    
    private ArrayList<Card> cards;
    private static final Random RANDOM = new Random();
    private static final CardSet madeDeck = new CardSet(52);
    static {
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Rank rank : Card.Rank.values())
                madeDeck.add(new Card(rank, suit));
    }

    /**
     * Return an ordered 52-card deck.
     * @return a 52-card deck in order from clubs to spades and within each suit from deuce to Ace.
     */
    public static CardSet freshDeck( ) {
        return new CardSet(madeDeck);
    }

    /**
     * Return a shuffled 52-card deck.
     * @return a shuffled 52-card deck.
     */
    public static CardSet shuffledDeck() {
        CardSet result = new CardSet(madeDeck);
        Collections.shuffle(result.cards);
        return result;
    }

    public CardSet() {
        cards = new ArrayList<Card>();
    }

    public CardSet(int initialCapacity) {
        cards = new ArrayList<Card>(initialCapacity);
    }

    /**
     * Copy constructor
     */
    public CardSet(CardSet source) {
        cards = new ArrayList<Card>(source.cards);
    }

    /**
     * Returns <code>true</code> if this CardSet did not already contain the specified Card.
     * @return <code>true</code> if this CardSet did not already contain the specified Card.
     */
    public boolean add(Card c) {
        if (cards.contains(c))
            return false;
        return cards.add(c);
    }
    
    /**
     * Returns <code>true</code> if this CardSet changed as a result of the call.
     * @return <code>true</code> if this CardSet changed as a result of the call; <code>false</code>
     * if all of the Cards in the specified Collection were already present in this CardSet.
     */
    public boolean addAll(Collection<? extends Card> coll) {
        boolean result = false;
        for (Card c : coll)
            result |= add(c);
        return result;
    }

    public void clear() {
        cards.clear();
    }

    public boolean contains(Object o) {
        return cards.contains(o);
    }

    public boolean containsAll(Collection<?> coll) {
        return cards.containsAll(coll);
    }

    @Override public boolean equals(Object that) {
        if (!(that instanceof Set) || ((Set)that).size() != cards.size())
            return false;
        for (Card c : cards)
            if (!((Set)that).contains(c))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Card c : cards)
            result += c.hashCode();
        return result;
    }
    
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Iterator<Card> iterator() {
        return cards.iterator();
    }

    public boolean remove(Object o) {
    	if (!(o instanceof Card)){
    		throw new RuntimeException("You can remove only cards from deck! "+o.getClass()+" is not Card at all");
    	}
        return cards.remove(o);
    }
    
    public boolean remove(CardSet cardSet){
    	return cards.removeAll(cardSet.cards);
    }

    public boolean removeAll(Collection<?> coll) {
        return cards.removeAll(coll);
    }
    
    public boolean removeAll(Card[] coll) {
    	boolean result = true;
    	for (Card c1 : coll){
    		result &= !cards.remove(c1);
    	}
        return result;
    }

    public boolean retainAll(Collection<?> coll) {
        return cards.retainAll(coll);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public int size() {
        return cards.size();
    }

    public Object[] toArray() {
        return cards.toArray(new Card[cards.size()]);
    }
    
    public <T> T[] toArray(T[] a) {
        return cards.toArray(a);
    }

    /**
     * @return one card from deck. extract this card from deck
     */
    public Card dealCard(){
    	return cards.remove(RANDOM.nextInt(cards.size()));
    }
    
    public Card get(int i){
    	return cards.get(i);
    }
    /**
     * @return a {@link String} containing a comma-space-separated list of cards,
     *          each the result of {@link Card#toString()}.
     */
    @Override
    public String toString() {
        return cards.toString();
    }
}
