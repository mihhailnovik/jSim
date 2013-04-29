package mi.poker.tests.calculation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.tests.BaseTest;

public class CardSetTest extends BaseTest {

	@Test
	public void testDeal(){ // testing randomness..
		int trials = 10000000;
		Map<Card, Integer> myMap = new HashMap<Card, Integer>();
		CardSet deck = CardSet.freshDeck();
		for (Card c : deck){
			myMap.put(c, 0);
		}
		for (int i = 0;i<trials;i++) {
			CardSet set = CardSet.freshDeck();
			Card c = set.dealCard();
			myMap.put(c,myMap.get(c)+1);
		}
		int biggest = 0;
		int lowest = -1;
		for (Card c : deck){
			if (lowest == -1){
				lowest = myMap.get(c);
			}
			if (biggest < myMap.get(c)){
				biggest = myMap.get(c);
			}
			
			if (lowest > myMap.get(c)){
				lowest = myMap.get(c);
			}
		}
		assertTrue(((double)(biggest - lowest)/(double)trials) < 0.001d);
	}
}