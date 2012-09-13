/**
 * @author m1
 */
package mi.poker.calculation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.common.model.testbed.klaatu.HandEval;
import mi.poker.common.utils.HandUtil;

public class MonteCarloSimulation implements Calculation {

	@Override
	public Result calculate(String playerHands, String boardCards,
			String deadCards) {
		Result result = new Result(playerHands);
		CardSet hands[][] = HandParser.parsePlayersHands(playerHands);// get all possible cards for players
		CardSet currentHands[] = new CardSet[hands.length]; // actual hands for player will be stored here
		// i need this list for random iterating over players, because there's no order which player should be dealt first
		List<Integer> exactlyTypeId = new LinkedList<Integer>(); // when we always have same card
		List<Integer> randomHandId = new LinkedList<Integer>();
		for (int i = 0;i<hands.length;i++){
			if (hands[i].length == 1){ // if only one hand is possible
				exactlyTypeId.add(i);
				currentHands[i] = hands[i][0];
			}
			if (hands[i].length == 1326) {
				randomHandId.add(i);
			}
		}
		List<Integer> orderList = new LinkedList<Integer>();
		for (int i = 0;i<hands.length;i++) {
			if (!exactlyTypeId.contains(i) && !randomHandId.contains(i)){ // no need random order known and random
				orderList.add(i); 
			}
		}
		Card[] deadCardsArray = HandParser.parseCards(deadCards);
		Card[] boardCardsArray = HandParser.parseCards(boardCards);
		
		CardSet staticBoard = new CardSet();
		for (Card c : boardCardsArray){
			staticBoard.add(c);
		}
		CardSet baseDeck = CardSet.freshDeck();
		baseDeck.removeAll(deadCardsArray);// delete dead cards
		baseDeck.removeAll(boardCardsArray); // delete board cards
		for (int j : exactlyTypeId){
			baseDeck.remove(currentHands[j]);
		}
		return calculateMonteCarlo(orderList,randomHandId,baseDeck,currentHands,staticBoard,result,hands,1000000);// FIXME 1000000 ?
	}
	
	public Result calculateMonteCarlo(List<Integer> rangeHandId,List<Integer> randomHandId, CardSet baseDeck,
			CardSet currentHands[], CardSet staticBoard, Result result,CardSet possibleHands[][] ,int trials) {
		
	
		for (int i = 0;i<trials;i++){ // lets simulate
			Collections.shuffle(rangeHandId);
			CardSet currentDeck = new CardSet(baseDeck);
			for (int j : rangeHandId){ // deal cards to range
				currentHands[j]  = CardDistributionUtil.extractRandomPossibleCard(possibleHands[j], currentDeck);
			}
			
			for (int j : randomHandId){
				currentHands[j]  = CardDistributionUtil.extractRandomPossibleCard(possibleHands[j], currentDeck);
			}
			
			CardSet board = new CardSet(staticBoard);
			
			while (board.size() < 5) { // deal cards to board, if needed
				board.add(currentDeck.dealCard());
			}
			// now calculate the result for eachPlayer
			for (int z = 0;z<currentHands.length;z++){
				result.getMap().get(z).setCurrentGameScore(HandEval.hand7Eval(HandUtil.mergeCardSet(board,currentHands[z]))); // saving hand strength
			}
			result.applyGameResult();
		}
		result.calculateStatistic();
		return result;
	}

}
