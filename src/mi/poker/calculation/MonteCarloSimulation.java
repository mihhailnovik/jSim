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
import mi.poker.common.model.testbed.spears2p2.Hand;
import mi.poker.common.model.testbed.spears2p2.StateTableEvaluator;
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
//		return calculateMonteCarlo(orderList,randomHandId,baseDeck,currentHands,staticBoard,result,hands,1000000);// FIXME 1000000 ?
		return calculateMonteCarloRayW(orderList,randomHandId,baseDeck,currentHands,staticBoard,result,hands,1000000);// FIXME 1000000 ?
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
/*				CardSet cards = HandUtil.mergeCardSet(board,currentHands[z]);
                                int i0 = cards.get(0).hashCode();
                                int i1 = cards.get(1).hashCode();
                                int i2 = cards.get(2).hashCode();
                                int i3 = cards.get(3).hashCode();
                                int i4 = cards.get(4).hashCode();
                                int i5 = cards.get(5).hashCode();
                                int i6 = cards.get(6).hashCode();
                                result.getMap().get(z).setCurrentGameScore(PartialStageFastEval.eval7(i0, i1, i2, i3, i4, i5, i6)); // saving hand strength
*/			}
			result.applyGameResult();
		}
		result.calculateStatistic();
		return result;
	}

	public Result calculateMonteCarloRayW(List<Integer> rangeHandId,List<Integer> randomHandId, CardSet baseDeck,
			CardSet currentHands[], CardSet staticBoard, Result result,CardSet possibleHands[][] ,int trials) {
		
            StateTableEvaluator.initialize();
            
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
				CardSet cards = HandUtil.mergeCardSet(board,currentHands[z]);
                                int i0 = cards.get(0).hashCode();
                                int i1 = cards.get(1).hashCode();
                                int i2 = cards.get(2).hashCode();
                                int i3 = cards.get(3).hashCode();
                                int i4 = cards.get(4).hashCode();
                                int i5 = cards.get(5).hashCode();
                                int i6 = cards.get(6).hashCode();
                                Hand h = new Hand();
                                h.addCard(i0);
                                h.addCard(i1);
                                h.addCard(i2);
                                h.addCard(i3);
                                h.addCard(i4);
                                h.addCard(i5);
                                h.addCard(i6);
                                
                                result.getMap().get(z).setCurrentGameScore(StateTableEvaluator.getRank(h)); // saving hand strength
			}
			result.applyGameResult();
		}
		result.calculateStatistic();
		return result;
	}

}
