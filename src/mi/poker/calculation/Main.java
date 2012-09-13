/**
 * @author m1
 */
package mi.poker.calculation;

public class Main {

	public static void main(String[] args){
                long start = System.currentTimeMillis();
		Result result = EquityCalculation.calculateMonteCarlo("KK,AKo,9s8s,3h3s","4h8hQc","2c3c3d");
                //Result result = EquityCalculation.calculate("AhAd,KK+|AKs|AKo","","");
                //Result result = EquityCalculation.calculateExhaustiveEnumration("AhKd,9c8c,3h3s","","");
                
                long end = System.currentTimeMillis();
                end = end - start;
		System.out.println(result);
                System.out.println("\n" + "time: " + end);
	}
}
