/**
 * @author m1
 */
package mi.poker.calculation;

public class Main {

	public static void main(String[] args){
		Result r = EquityCalculation.calculate("AsQd,JJ", "5sTs7c", "");
		System.out.println(r);
	}
}
