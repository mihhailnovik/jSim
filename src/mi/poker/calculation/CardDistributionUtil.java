package mi.poker.calculation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.common.utils.CollectionUtil;

/**
 * @author m1
 */
public class CardDistributionUtil {

	/**
	 * Randomly pick's on of possible hands, and if cards from this hand are
	 * available in the deck then extract them from deck and returns this hand
	 * Extract picked card from deck 
	 */
	public static CardSet extractRandomPossibleCard(CardSet[] possibleHands, CardSet deck) {
		if (possibleHands.length == 1326){ // all hands are possible, so random
			CardSet result = new CardSet();
			result.add(deck.dealCard());
			result.add(deck.dealCard());
			return result;
		}
		List<CardSet> hands = CollectionUtil.buildListFromArray(possibleHands);
		Collections.shuffle(hands); // we need random order
		for (CardSet h : hands) {
			Card card1 = h.get(0);
			Card card2 = h.get(1);
			if (deck.contains(card1)&& deck.contains(card2)) { // if cards are still in deck 
				deck.remove(card1);
				deck.remove(card2);
				return h;
			}
		}
		throw new RuntimeException("This is sad. Possible hands is - "+Arrays.toString(possibleHands) +" but deck is"+deck);
	}
	
	/**
	 * @return all possible hands from fresh deck, 1326 hands
	 */
	public static CardSet[] getAllPossibleHands(){
		CardSet deck1 = CardSet.freshDeck();
		CardSet[] cardSet = new CardSet[1326];
		int counter = 0;
		for (int i = 0;i < 51;i++){
			for (int j = i+1;j<52;j++){
				CardSet set = new CardSet();
				set.add(deck1.get(i));
				set.add(deck1.get(j));
				cardSet[counter++] = set;
			}
		}
		return cardSet;
	}
	
}
