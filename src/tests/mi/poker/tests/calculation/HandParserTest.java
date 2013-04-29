package mi.poker.tests.calculation;

import org.junit.Test;
import mi.poker.calculation.CardUtil;
import mi.poker.calculation.HandParser;
import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.tests.BaseTest;

public class HandParserTest extends BaseTest {

	@Test
	public void testGetType() {
		assertEquals(HandParser.RANGE_TYPE, HandParser.getType("ATs+"));
		assertEquals(HandParser.RANGE_TYPE, HandParser.getType("99+"));
		assertEquals(HandParser.PATTERN_SUIT_TYPE, HandParser.getType("99"));
		assertEquals(HandParser.EXACTLY_TYPE, HandParser.getType("9d9s"));
		assertEquals(HandParser.PATTERN_SUIT_TYPE, HandParser.getType("Q9s"));
		assertEquals(HandParser.RANDOM_TYPE, HandParser.getType("XxXx"));
	}

	@Test
	public void testGetPlayersHands() {
		CardSet[][] hands = HandParser
				.parsePlayersHands("JcJh,8s7s,99+|AJs+,QQ+|AQs+|AQo+,XxXx,XxXx,XxXx,XxXx");
		assertEquals(hands.length, 8);
		// first player
		assertEquals(hands[0].length, 1);
		assertTrue(hands[0][0].get(0).equals(CardUtil.buildCard('J', 'c'))
				&& hands[0][0].get(1).equals(CardUtil.buildCard('J', 'h')));
		// second player
		CardSet secondPlayerHand = hands[1][0];
		assertTrue(secondPlayerHand.get(0).equals(CardUtil.buildCard('8','s'))
				&& secondPlayerHand.get(1).equals(CardUtil.buildCard('7','s')));
		//thirth player
		CardSet[] thirthPlayerHands = hands[2];
		assertEquals(thirthPlayerHands.length, 10);
		
		//fourth player
		CardSet[] fourthPlayerHands = hands[3];
		assertEquals(fourthPlayerHands.length, 22);
	}

	@Test
	public void testGetPossibleHands() {
		assertTrue(HandParser.parsePossibleHands("QQ+|AQs+|AQo+").length == 22);
	}
	
	@Test
	public void testParseCards(){
		String cards = "QhJs8sAhAcAd";
		Card[] parsedCards = HandParser.parseCards(cards);
		assertEquals(parsedCards[0].rankOf().toChar(), 'Q');
		assertEquals(parsedCards[1].rankOf().toChar(), 'J');
		assertEquals(parsedCards[2].rankOf().toChar(), '8');
		assertEquals(parsedCards[3].rankOf().toChar(), 'A');
		assertEquals(parsedCards[4].rankOf().toChar(), 'A');
		assertEquals(parsedCards[5].rankOf().toChar(), 'A');
		
		assertEquals(parsedCards[0].suitOf().toChar(), 'h');
		assertEquals(parsedCards[1].suitOf().toChar(), 's');
		assertEquals(parsedCards[2].suitOf().toChar(), 's');
		assertEquals(parsedCards[3].suitOf().toChar(), 'h');
		assertEquals(parsedCards[4].suitOf().toChar(), 'c');
		assertEquals(parsedCards[5].suitOf().toChar(), 'd');
	}

}
