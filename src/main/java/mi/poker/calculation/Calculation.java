/**
 * @author m1
 */
package mi.poker.calculation;

/**
 * Base interface for calculation process
 */
public interface Calculation {
	/**
	 * No guaranty if your input is impossible.
	 * @param playerHands, can be range of cards (sample "QQ", "KSo","99+") separated by ',', no spaces
	 * valid sample "QsTd,99,99+,AsAd"
	 * @param boardCards strict format "As3cTh", no spaces, no coma
	 * @param deadCards same format as board
	 * @return
	 */
	public Result calculate(String playerHands, String boardCards,
			String deadCards);
}
