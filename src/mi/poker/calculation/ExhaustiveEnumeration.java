package mi.poker.calculation;

import java.util.LinkedList;
import java.util.List;

import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.common.model.testbed.klaatu.HandEval;
import mi.poker.common.utils.HandUtil;
/**
 * @author m1
 */
public class ExhaustiveEnumeration implements Calculation {

	private CardSet[][] possibleHands;
	private CardSet board;
	private CardSet deadCards;
	private Result result;

	public Result calculate(String playerHands, String boardCards,
			String deadCards) {
		result = new Result(playerHands);
		this.possibleHands = HandParser.parsePlayersHands(playerHands);
		this.board = HandUtil.mergeCards(HandParser.parseCards(boardCards));
		this.deadCards = HandUtil.mergeCards(HandParser.parseCards(deadCards));

		CardSet deck = CardSet.freshDeck();
		deck.removeAll(board);
		deck.removeAll(this.deadCards);
		enumerate(0, new LinkedList<CardSet>(), deck); // starting enumeration from player 0
		result.calculateStatistic();
		return result;
	}

	/**
	 * @param currentPlayer - number of player we deal cards
	 * @param playersHands - other players hands
	 * @param deck - deck
	 */
	private void enumerate(int currentPlayer, List<CardSet> playersHands,
			CardSet deck) {
		CardSet[] currentPossibleHands = possibleHands[currentPlayer]; // possible hands form player 'currentPlayer'
		
		for (int i =0;i<currentPossibleHands.length;i++) { // loop for all possible hands
			if (!deck.contains(currentPossibleHands[i].get(0)) 
					|| !deck.contains(currentPossibleHands[i].get(1))) { // if hand is impossible (no such cards in deck), then we took next one
				continue;
			}
			
			playersHands.add(currentPossibleHands[i]); // this is our current hands for this player
			deck.remove(currentPossibleHands[i].get(0)); // extract cards from deck
			deck.remove(currentPossibleHands[i].get(1)); 

			if (currentPlayer + 1 < possibleHands.length) { // if there is other player without dealt cards
				enumerate(currentPlayer + 1, playersHands, deck); // deal cards to next player
				
				playersHands.remove(currentPossibleHands[i]);
				deck.add(currentPossibleHands[i].get(0));
				deck.add(currentPossibleHands[i].get(1));
				continue;
			} else {
				// if we are here it means all player are with cards, so we can enumerate board dealing
				dealBoardAndPlay(playersHands, board,deck,0);
				playersHands.remove(currentPossibleHands[i]);
				deck.add(currentPossibleHands[i].get(0));
				deck.add(currentPossibleHands[i].get(1));
			}
		}
	}
	
	private void dealBoardAndPlay(List<CardSet> playersHands, CardSet board, CardSet deck,int cardIndex){
		if (board.size() == 5){ // if board is full
			calculateResult(playersHands,board);
			return;
		}
		int deckSize = deck.size();
		for (int i = cardIndex;i<deckSize;i++){ 
			board.add(deck.get(i));
			if (board.size() != 5){
				dealBoardAndPlay(playersHands,board,deck,i+1);
				board.remove(deck.get(i));
				continue;
			}
			calculateResult(playersHands,board);
			board.remove(deck.get(i));
		}
	}
	
	private void calculateResult(List<CardSet> playersHands, CardSet board){
		for (int i =0;i<playersHands.size();i++){
			result.getMap().get(i).setCurrentGameScore(HandEval.hand7Eval(HandUtil.mergeCardSet(board,playersHands.get(i))));
		}
		result.applyGameResult();
	}
	
	public CardSet[][] getPossibleHands() {
		return possibleHands;
	}

	public CardSet getBoard() {
		return board;
	}

	public Result getResult() {
		return result;
	}

	public void setPossibleHands(CardSet[][] possibleHands) {
		this.possibleHands = possibleHands;
	}

	public void setBoard(CardSet board) {
		this.board = board;
	}

	public void setResult(Result result) {
		this.result = result;
	}
}
