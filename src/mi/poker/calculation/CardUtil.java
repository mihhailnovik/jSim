package mi.poker.calculation;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.Rank;
import mi.poker.common.model.testbed.klaatu.Suit;

/**
 * @author m1
 * Helper for testbed.klaatu.Card
 */
public class CardUtil {

	public static Card buildCard(char rank, char suit){
		return new Card(Rank.fromChar(rank),Suit.fromChar(suit));
	}
	/**
	 * @param rank 0 - 11
	 * @param suit 0 - 3
	 * 0 - CLUB
	 * 1 - DIAMOND
	 * 2 - HEART
	 * 3 - SPADE
	 * @return 
	 */
	public static Card buildCard(final int rank, int suit){
		Rank r = null;
		Suit s = null;
		switch (rank) {
			case 0: {
				r = Rank.TWO;
				break;
			}
			case 1: {
				r = Rank.THREE;
				break;
			}
			case 2: {
				r = Rank.FOUR;
				break;
			}
			case 3: {
				r = Rank.FIVE;
				break;
			}
			case 4: {
				r = Rank.SIX;
				break;
			}
			case 5: {
				r = Rank.SEVEN;
				break;
			}
			case 6: {
				r = Rank.EIGHT;
				break;
			}
			case 7: {
				r = Rank.NINE;
				break;
			}
			case 8: {
				r = Rank.TEN;
				break;
			}
			case 9: {
				r = Rank.JACK;
				break;
			}
			case 10: {
				r = Rank.QUEEN;
				break;
			}
			case 11: {
				r = Rank.KING;
				break;
			}
			case 12: {
				r = Rank.ACE;
				break;
			}
		}
		switch (suit){
			case 0:{
				s = Suit.CLUB;
				break;
			}
			case 1:{
				s = Suit.DIAMOND;
				break;
			}
			case 2:{
				s = Suit.HEART;
				break;
			}
			case 3:{
				s = Suit.SPADE;
				break;
			}
		}
		return new Card(r, s);
	}
}
