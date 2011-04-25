/**
 * @author m1
 */
package mi.poker.calculation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Result {

	private List<Integer> topScorePlayerIdList = new LinkedList<Integer>();
	private long topScore = 0;
	private Map<Integer, HandInfo> map = new HashMap<Integer, HandInfo>();

	public Result(String playerHands) {
		playerHands = playerHands.trim();
		String[] handsArray = playerHands.split(",");
		int i = 0;
		for (String hand : handsArray) {
			map.put(i++, new HandInfo(hand));
		}
	}

	public HandInfo getHandInfo(int playerNr) {
		return map.get(playerNr);
	}

	public Map<Integer, HandInfo> getMap() {
		return map;
	}

	public void setMap(Map<Integer, HandInfo> map) {
		this.map = map;
	}

	/**
	 * detects winner, and updates stats
	 */
	public void applyGameResult() {
		topScore = 0;
		topScorePlayerIdList.clear();
		Set<Entry<Integer, HandInfo>> entries = map.entrySet();
		for (Entry<Integer, HandInfo> entry : entries) {
			if (entry.getValue().getCurrentGameScore() > topScore) { // if we
																		// have
																		// score
																		// better
																		// then
																		// previous
																		// best
																		// score
				topScore = entry.getValue().getCurrentGameScore(); // then it's
																	// a our new
																	// top score
				topScorePlayerIdList.clear(); // we do not care about losers
												// anymore
				topScorePlayerIdList.add(entry.getKey()); // we have our own
															// current leader
			} else {
				if (entry.getValue().getCurrentGameScore() == topScore) { // if
																			// we
																			// have
																			// the
																			// same
																			// score
					topScorePlayerIdList.add(entry.getKey());// it's a draw then
				}
			}

			entry.getValue().increaseTotalGame(); // everyone played a game
			entry.getValue().setCurrentGameScore(0); // do not care about
														// playerScore anymore
		}

		if (topScorePlayerIdList.size() == 1) { // we got a winner :)
			HandInfo winnerHand = map.get(topScorePlayerIdList.get(0));
			winnerHand.increasePotsWon();
		} else { // draw
			for (int i : topScorePlayerIdList) {
				HandInfo tightHand = map.get(i);
				tightHand.increasePotsTied();
				tightHand.addToPotsWonTied((double) 1
						/ (double) topScorePlayerIdList.size());
			}
		}
	}

	public void calculateStatistic() {
		Set<Entry<Integer, HandInfo>> entries = map.entrySet();
		for (Entry<Integer, HandInfo> entry : entries) {
			entry.getValue().calculateStatistic();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Set<Entry<Integer, HandInfo>> entries = map.entrySet();
		for (Entry<Integer, HandInfo> entry : entries) {
			builder.append(entry.getValue().toString()).append("\n");
		}
		return builder.toString();
	}
}
