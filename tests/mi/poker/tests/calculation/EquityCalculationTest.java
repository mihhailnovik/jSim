package mi.poker.tests.calculation;

import org.junit.Test;

import junit.framework.TestCase;
import mi.poker.calculation.EquityCalculation;
import mi.poker.calculation.HandInfo;
import mi.poker.calculation.MonteCarloSimulation;
import mi.poker.calculation.Result;

public class EquityCalculationTest extends TestCase {

	private static double IN_RANGE_NUMBER = 0.1d;
	
	@Test
	public void test1(){
		Result result = EquityCalculation.calculate("JcJh,8s7s", 
				"4dAc5d", "");
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),80.5d));
		assertTrue(almostEqualPercent(p1.getWin(), 79.7));
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),19.4));
		assertTrue(almostEqualPercent(p2.getWin(),18.7));
	}
	
	@Test
	public void test2(){
		Result result = EquityCalculation.calculate("KK,AKo,9s8s,3h3s","4h8hQc","2c3c3d");
	
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		HandInfo p3 = result.getHandInfo(2);
		HandInfo p4 = result.getHandInfo(3);
	
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),61.76d));
		assertTrue(almostEqualPercent(p1.getWin(), 61.77));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),16.54));
		assertTrue(almostEqualPercent(p2.getWin(),16.55));
	
		//player 3
		assertTrue(almostEqualPercent(p3.getEquity(),20.64));
		assertTrue(almostEqualPercent(p3.getWin(),20.65));
	
		//player 4
		assertTrue(almostEqualPercent(p4.getEquity(),1.04));
		assertTrue(almostEqualPercent(p4.getWin(),1.05));
	
	}
	
	@Test
	public void test3(){
		Result result = EquityCalculation.calculate("6c3d,5d5h", 
				"AhKdKc", "");
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),21.58d));
		assertTrue(almostEqualPercent(p1.getWin(), 15.18));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),78.41));
		assertTrue(almostEqualPercent(p2.getWin(),72.02));
	}

	@Test
	public void test4(){
		Result result = EquityCalculation.calculate("AcAh,7d2d", 
				"", "");
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),83.29d));
		assertTrue(almostEqualPercent(p1.getWin(), 83.11));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),16.7));
		assertTrue(almostEqualPercent(p2.getWin(),16.5));
	}
	
	@Test
	public void test5(){
		Result result = EquityCalculation.calculate("AcAh,7d2d,5d4d,6s4s", 
				"", "AdAsKsKhKdKcQcQdQhQsJsJhJd");
		
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		HandInfo p3 = result.getHandInfo(2);
		HandInfo p4 = result.getHandInfo(3);
	
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),38.37));
		assertTrue(almostEqualPercent(p1.getWin(), 38.17));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),19.29));
		assertTrue(almostEqualPercent(p2.getWin(),19.09));
	
		//player 3
		assertTrue(almostEqualPercent(p3.getEquity(),17.37));
		assertTrue(almostEqualPercent(p3.getWin(),15.33));
	
		//player 4
		assertTrue(almostEqualPercent(p4.getEquity(),24.95));
		assertTrue(almostEqualPercent(p4.getWin(),22.92));
	}
	
	@Test
	public void test6(){
		Result result = EquityCalculation.calculate("AA,KK,QQ,JJ,TT,99,88,77,66,55", 
				"", "4d4c2s3s4s4h3h2h2d3d3c2c");
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		HandInfo p3 = result.getHandInfo(2);
		HandInfo p4 = result.getHandInfo(3);
		HandInfo p5 = result.getHandInfo(4);
		HandInfo p6 = result.getHandInfo(5);
		HandInfo p7 = result.getHandInfo(6);
		HandInfo p8 = result.getHandInfo(7);
		HandInfo p9 = result.getHandInfo(8);
		HandInfo p10 = result.getHandInfo(9);
		
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),24.56));
		assertTrue(almostEqualPercent(p1.getWin(), 24.54));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),15.74));
		assertTrue(almostEqualPercent(p2.getWin(),15.73));
	
		//player 3
		assertTrue(almostEqualPercent(p3.getEquity(),10.99));
		assertTrue(almostEqualPercent(p3.getWin(),10.98));
	
		//player 4
		assertTrue(almostEqualPercent(p4.getEquity(),9.23));
		assertTrue(almostEqualPercent(p4.getWin(),9.21));
		
		//player 5
		assertTrue(almostEqualPercent(p5.getEquity(),9.2));
		assertTrue(almostEqualPercent(p5.getWin(), 9.18));
		
		//player 6
		assertTrue(almostEqualPercent(p6.getEquity(),7.86));
		assertTrue(almostEqualPercent(p6.getWin(),7.85));
	
		//player 7
		assertTrue(almostEqualPercent(p7.getEquity(),6.92));
		assertTrue(almostEqualPercent(p7.getWin(),6.9));
	
		//player 8
		assertTrue(almostEqualPercent(p8.getEquity(),6));
		assertTrue(almostEqualPercent(p8.getWin(),5.99));
		
		//player 9
		assertTrue(almostEqualPercent(p9.getEquity(),5.07));
		assertTrue(almostEqualPercent(p9.getWin(),5.05));
	
		//player 10
		assertTrue(almostEqualPercent(p10.getEquity(),4.38));
		assertTrue(almostEqualPercent(p10.getWin(),4.36));
	}
	
	@Test
	public void test7(){
		double exInRange = IN_RANGE_NUMBER;
		IN_RANGE_NUMBER = 0.2;
		Result result = EquityCalculation.calculate("XxXx,XxXx", 
				"", "");
		HandInfo p1 = result.getHandInfo(0);
		HandInfo p2 = result.getHandInfo(1);
		//player1
		assertTrue(almostEqualPercent(p1.getEquity(),49.98d));
		assertTrue(almostEqualPercent(p1.getWin(), 47.95));
		
		//player 2
		assertTrue(almostEqualPercent(p2.getEquity(),50));
		assertTrue(almostEqualPercent(p2.getWin(),47.95));
		IN_RANGE_NUMBER = exInRange;
	}
	
	private boolean almostEqual(double number, double number2){
		return number > number2 - IN_RANGE_NUMBER && number < number2 + IN_RANGE_NUMBER;
	}
	
	private boolean almostEqualPercent(double percentNumber, double number2){
		return almostEqual(percentNumber*100, number2);
	}
}
