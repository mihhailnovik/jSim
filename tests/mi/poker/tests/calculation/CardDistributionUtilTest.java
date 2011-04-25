package mi.poker.tests.calculation;
import mi.poker.calculation.CardDistributionUtil;
import mi.poker.calculation.HandParser;
import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.tests.BaseTest;

import org.junit.Test;

public class CardDistributionUtilTest extends BaseTest{

	@Test
	public void testGetPossibleHand(){
		CardSet[] possibleHands = HandParser.parsePossibleHands("QQ+|AQs+|AQo+");
		CardSet deck = CardSet.freshDeck();
		Card[] removedCards = HandParser.parseCards("QhQdQsAdAsAc");
		deck.removeAll(removedCards);
		CardSet h = CardDistributionUtil.extractRandomPossibleCard(possibleHands, deck);
		assertEquals(h.get(0).rankOf().ordinal(), 12);//Ace
		assertEquals(h.get(0).suitOf().ordinal(), 2);//hearts
		assertEquals(h.get(1).rankOf().ordinal(), 10);//Queen
		assertEquals(h.get(1).suitOf().ordinal(), 0);//clubs
	}
	
	@Test
	public void testGetAllPossibleHands(){
		assertEquals(CardDistributionUtil.getAllPossibleHands().length, 1326);
	}
}
