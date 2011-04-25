/**
 * @author m1
 */
package mi.poker.calculation;

import java.util.LinkedList;
import java.util.List;

import mi.poker.common.model.testbed.klaatu.CardSet;

public class BaseRangeStrategy extends RangeStrategy {

	@Override
	public CardSet[] getRange(String card) {
		card = card.substring(0, card.length()-1);
		List<String> included = new LinkedList<String>();
		for (String hand : handRankingStr){
			included.add(hand);
			if (hand.equals(card)){
				break;
			}
		}
		
		List<CardSet> cardSets = new LinkedList<CardSet>();
		for (String hand : included){
			CardSet[] result = HandParser.parsePatternSuit(hand);
			for (CardSet set : result){
				cardSets.add(set);
			}
		}
		CardSet[] result = new CardSet[cardSets.size()];
		int i = 0;
		for (CardSet cs : cardSets){
			result[i++] = cs;
		}
		return result;
	}

	// thank you to quentince from pokerai.org forum
	public static String[] handRankingStr = { "AA", "KK", "AKs", "QQ", "AK",
			"JJ", "AQs", "TT", "AQ", "99", "AJs", "88", "ATs", "77", "66",
			"KQs", "KJs", "KTs", "QJs", "QTs", "JTs", "55", "44", "A9s", "AJ",
			"A5s", "KQ", "K9s", "T9s", "33", "J9s", "KJ", "Q9s", "22", "A8s",
			"A4s", "AT", "A7s", "QJ", "98s", "A3s", "K8s", "Q8s", "J8s", "T8s",
			"A6s", "A9", "K7s", "A2s", "JT", "A8", "QT", "87s", "A5", "KT",
			"A7", "K6s", "97s", "A6", "A4", "T7s", "76s", "A3", "A2", "J7s",
			"K5s", "86s", "T9", "65s", "Q6s", "96s", "K4s", "Q7s", "J9", "K9",
			"75s", "K3s", "54s", "K2s", "Q5s", "T6s", "Q9", "K8", "98", "K7",
			"85s", "K6", "J6s", "64s", "Q4s", "T8", "J5s", "K5", "Q3s", "K4",
			"Q8", "95s", "K3", "87", "Q2s", "K2", "J4s", "J8", "74s", "T5s",
			"Q7", "Q6", "J3s", "97", "76", "Q5", "T4s", "J2s", "Q4", "J7",
			"T7", "Q3", "84s", "Q2", "T3s", "J6", "T2s", "J5", "J4", "86",
			"T6", "94s", "J3", "96", "J2", "93s", "T5", "T4", "95", "92s",
			"T3", "T2", "53s", "85", "83s", "94", "75", "82s", "73s", "93",
			"65", "63s", "84", "92", "43s", "74", "72s", "54", "64", "52s",
			"62s", "83", "42s", "82", "73", "53", "63", "32s", "43", "72",
			"52", "62", "42", "32" };

}
