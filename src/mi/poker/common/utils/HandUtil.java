package mi.poker.common.utils;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;

/**
 * @author m1
 */
public class HandUtil {

	/**
	 * @param card1Str
	 * @param card2Str
	 * @return normalized (stronger card first) hand
	 */
	public static CardSet buildTwoCardHand(String card1Str, String card2Str){
		Card card1 = new Card(card1Str);
		Card card2 = new Card(card2Str);
		
		return buildTwoCardHand(card1,card2);
	}
	
	public static CardSet buildTwoCardHand(Card card1, Card card2){
		CardSet hand = new CardSet();
		if (card1.rankOf().ordinal() >= card2.rankOf().ordinal()){
			hand.add(card1);
			hand.add(card2);
		}
		else {
			hand.add(card2);
			hand.add(card1);
		}
		return hand;
	}
	
	public static CardSet mergeCardSet(CardSet... cardSet){
		CardSet set = new CardSet();
		for (CardSet s : cardSet){
			set.addAll(s);
		}
		return set;
	}
	
	public static CardSet mergeCards(Card[] cards){
		CardSet result = new CardSet();
		for (Card c : cards){
			result.add(c);
		}
		return result;
	}
}
