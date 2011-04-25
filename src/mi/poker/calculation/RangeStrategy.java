package mi.poker.calculation;

import mi.poker.common.model.testbed.klaatu.CardSet;

/**
 * @author m1
 * Defines strategy for card range.
 */
public abstract class RangeStrategy {

	/**
	 * @param card
	 * @return possible hands
	 * defines ranges 
	 * sample: if card = "QQ+" in some scenarios AKs may be included in this range, in some not
	 */
	public abstract CardSet[] getRange(String card);
	
	public static RangeStrategy getDefaultRangeStrategy(){
		return new BaseRangeStrategy();
	}
}
