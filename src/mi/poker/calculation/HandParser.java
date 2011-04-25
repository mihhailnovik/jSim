package mi.poker.calculation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.common.utils.CollectionUtil;
import mi.poker.common.utils.HandUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author m1
 * 
 * Utility class to get Card object's from String
 */
public class HandParser {
	public static final char[] SUITS = { 's', 'd', 'h', 'c' };
	public static final int EXACTLY_TYPE = 1;
	public static final int RANGE_TYPE = 2;
	public static final int PATTERN_SUIT_TYPE = 3;
	public static final int RANDOM_TYPE = 4;
	/**
	 * @param hands
	 *            , sample format JcJh, 8s7s,
	 *            99+|AJs+,QQ|AQs+|AQo,XxXx,XxXx,XxXx,XxXx
	 * @return double array of hands, with possible variations
	 */
	public static CardSet[][] parsePlayersHands(String hands) {
		hands = hands.trim();
		String[] handsArray = hands.split(",");
		CardSet[][] handsResult = new CardSet[handsArray.length][];
		for (int i = 0; i < handsArray.length; i++) {
			handsResult[i] = parsePossibleHands(handsArray[i]);
		}
		return handsResult;
	}
	
	/**
	 * @param  possible format is only "AdTsJsQd3h"
	 * @return cards array
	 */
	public static Card[] parseCards(String cards){
		cards = cards.replaceAll(" ", "");
		Card[] result = new Card[cards.length()/2];
		int j = 0;
		for (int i = 0;i<cards.length(); i += 2){
			result[j++] = CardUtil.buildCard(cards.charAt(i), cards.charAt(i+1));
		}
		return result;
	}

	/**
	 * return's a hand format type
	 * "9d9s" - EXACTLY_TYPE 
	 * "99+","ATs+","ATo+" - RANGE_TYPE 
	 * "ATo" - PATTERN_TYPE
	 * "XxXx" - RANGOM_TYPE
	 * public only because of junit test, so no need to use outside this class
	 */
	public static int getType(String variant) {
		variant = StringUtils.trim(variant);
		if (variant.endsWith("+")){
			return RANGE_TYPE;
		}
		int length = variant.length();
		if (length == 4) {
			// if second letter is lower case, and forth letter is lower case
			// it's a first type
			if (isSuit(variant.charAt(1)) && isSuit(variant.charAt(3))) {
				return EXACTLY_TYPE;
			}
			if ("XxXx".equals(variant)){
				return RANDOM_TYPE;
			}
		}
		if (length == 3 || length == 2) {
				return PATTERN_SUIT_TYPE;//ATo
		}
		throw new RuntimeException("Invalid variant = " + variant); 
	}

	/**
	 * sample inputs "JcJh","99+|AJs+","QQ+|AQs+|AQo+"
	 * returns all of possible hands for sppecified input
	 */
	public static CardSet[] parsePossibleHands(String possibleCards) {
		possibleCards = StringUtils.remove(possibleCards, " ");
		String[] variants = StringUtils.split(possibleCards,'|');
		
		Set<CardSet> hands = new HashSet<CardSet>();
		for (String variant : variants) {
			int type = getType(variant);
			switch (type) {
				case EXACTLY_TYPE:// AhTs
				{
					CardSet hand = HandUtil.buildTwoCardHand(variant.substring(0, 2), variant.substring(2, 4));
					hands.add(hand);
					break;
				}
	
				case PATTERN_SUIT_TYPE: // "TQo"
				{
					CollectionUtil.addAll(hands, parsePatternSuit(variant));
					break;
				}
				case RANGE_TYPE:// AJs+
				{
					CollectionUtil.addAll(hands,RangeStrategy.getDefaultRangeStrategy().getRange(variant));
					break;
				}
				case RANDOM_TYPE:{ 
					CollectionUtil.addAll(hands,CardDistributionUtil.getAllPossibleHands());
					break;
				}
			}
		}
		CardSet[] result = new CardSet[hands.size()];
		CollectionUtil.fillArray(result, new LinkedList<CardSet>(hands));
		return result;
	}
	
	// input sample "AA", "TT", "33"
	private static CardSet[] parsePair(String cardPattern){
		char rank = cardPattern.charAt(0);
		String[] cards = new String[4];
		int i = 0;
		for (char c : SUITS) {
			cards[i++] = String.valueOf(rank) + String.valueOf(c);
		}
		return new CardSet[] {
				HandUtil.buildTwoCardHand(cards[0],cards[1]),
				HandUtil.buildTwoCardHand(cards[0],cards[2]),
				HandUtil.buildTwoCardHand(cards[0],cards[3]),
				HandUtil.buildTwoCardHand(cards[1],cards[2]),
				HandUtil.buildTwoCardHand(cards[1],cards[3]),
				HandUtil.buildTwoCardHand(cards[2],cards[3])
		};
	}
	
	private static CardSet[] parseNonPair(String cardPattern){
		char suitedOrNot = cardPattern.charAt(2);
		if (suitedOrNot == 's'){
			CardSet tempHands[] = new CardSet[4];
			int i = 0;
			for (char c : SUITS) {
				tempHands[i++] = HandUtil.buildTwoCardHand(Character.toString(cardPattern.charAt(0)) + c, 
						Character.toString(cardPattern.charAt(1)) + c);
			}
			return tempHands;
		}
		
		if (suitedOrNot == 'o'){
			char firstCard = cardPattern.charAt(0);
			char secondCard = cardPattern.charAt(1);
			Card[] firstPossibleCardArray = new Card[4];
			Card[] secondPossibleCardArray = new Card[4];
			int i = 0;
			for (char c : SUITS) {
				firstPossibleCardArray[i] =  CardUtil.buildCard(firstCard,c);
				secondPossibleCardArray[i] = CardUtil.buildCard(secondCard,c);
				i++;
			}
			i = 0;
			CardSet tempHands[] = new CardSet[12];
			for (Card c1 : firstPossibleCardArray){
				for (Card c2 : secondPossibleCardArray) {
					if (c1.suitOf().ordinal() != c2.suitOf().ordinal()){
						tempHands[i++] = HandUtil.buildTwoCardHand(c1, c2);
					}
				}
			}
			return tempHands;
		}
		return null;
	}
	// return possible hands for formats like "AJo", "AJs","99"
 	public static CardSet[] parsePatternSuit(String cardPattern) {
		if (cardPattern.trim().length() == 2) { // pattern pair "99", "44"
			return parsePair(cardPattern);
		}
		
 		if (cardPattern.trim().length() == 3) { // "AJo"
			return parseNonPair(cardPattern);
		}
		throw new RuntimeException("Invalid parameter "+cardPattern);
 	}

	public static boolean isSuit(char letter) {
		return ArrayUtils.contains(SUITS, letter);
	}
}
