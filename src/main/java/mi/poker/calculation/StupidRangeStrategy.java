package mi.poker.calculation;

import mi.poker.common.model.testbed.klaatu.CardSet;

public class StupidRangeStrategy extends RangeStrategy {

	@Override
	/**
	 * treats range, like suited pattern so QTs+ == QTs, 99+ = 99
	 * in other words, just ignore '+'
	 */
	public CardSet[] getRange(String card) {
		return HandParser.parsePatternSuit(card.replace("+", ""));
	}
}
