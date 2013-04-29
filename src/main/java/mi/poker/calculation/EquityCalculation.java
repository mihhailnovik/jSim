/**
 * @author m1
 */
package mi.poker.calculation;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;

public class EquityCalculation {
	
	/**
	 * @param playerHands
	 * all players hand's separated by ','
	 * @param boardCards
	 * @param deadCards
	 * @return
	 */
	public static Result calculate(String playerHands, String boardCards,
			String deadCards){
		return getBestCalculation(playerHands, boardCards, deadCards).calculate(playerHands, boardCards, deadCards);
	}
	
	public static Result calculateMonteCarlo(String playerHands, String boardCards,
			String deadCards){
		return new MonteCarloSimulation().calculate(playerHands, boardCards, deadCards);
	}
	
	public static Result calculateExhaustiveEnumration(String playerHands, String boardCards,
			String deadCards){
		return new ExhaustiveEnumeration().calculate(playerHands, boardCards, deadCards);
	}
	
	/**
	 * Algorithm may work bad. Just simple implementation
	 * @param playerHands
	 * @param boardCards
	 * @param deadCards
	 * @return
	 */
	private static Calculation getBestCalculation(String playerHands, String boardCards,
			String deadCards){
		
		CardSet[][] possibleHands = HandParser.parsePlayersHands(playerHands);
		Card[] board = HandParser.parseCards(boardCards);
		int playerVariations = 1;
		int boardVariations = 5 - board.length;
		for (int i = 0;i< possibleHands.length;i++){
			playerVariations *= possibleHands[i].length;
		}
		
		if (boardVariations == 5 || boardVariations == 4 || boardVariations == 3){
			playerVariations *= 1000; // number from head
		}
		
		if (boardVariations == 2){
			playerVariations *= 700; // number from head
		}
		
		if (boardVariations == 1){
			playerVariations *= 50;
		}
		
		boolean monteCarlo = playerVariations > 5000;
		return monteCarlo ? new MonteCarloSimulation() : new ExhaustiveEnumeration(); 
		
	}
	
}
