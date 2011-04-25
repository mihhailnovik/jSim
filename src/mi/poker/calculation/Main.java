/**
 * @author m1
 */
package mi.poker.calculation;

public class Main {

	public static void main(String[] args){
		Result result = EquityCalculation.calculate("KK,AKo,9s8s,3h3s","4h8hQc","2c3c3d");
		System.out.println(result);
	}
}
