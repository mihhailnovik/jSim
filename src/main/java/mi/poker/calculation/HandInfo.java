package mi.poker.calculation;

import java.text.DecimalFormat;

/**
 * @author m1
 */
public class HandInfo {
	
	private double equity;
	private double win;
	private double tie;
	private int potsWon;
	private int potsTied;
	private String hand;
	private int totalGames;
	private double potsWonTied;
	private DecimalFormat df = new DecimalFormat("#.###");
	
	public HandInfo(String hand){
		this.hand = hand;
	}
	
	public void calculateStatistic(){
		win = (double)((double)potsWon  / (double)totalGames);
		tie = (double)((double)potsTied / (double)totalGames);
		equity = (double)((double)potsWonTied / (double)totalGames);
	}
	
	@Override
	public String toString(){
		return hand+" equity: "+df.format(equity*100)+" tie "+df.format(tie*100)+ " win "+df.format(win*100)+" potsWon "+potsWon+
				" potsTied "+potsTied+" totalGames "+totalGames;
	}
	
	// temp variable for saving gameScore
	private long currentGameScore;
	
	public double getEquity() {
		return equity;
	}
	public double getWin() {
		return win;
	}
	public double getTie() {
		return tie;
	}
	public int getPotsWon() {
		return potsWon;
	}
	public int getPotsTied() {
		return potsTied;
	}
	public String getHand() {
		return hand;
	}
	public void setEquity(double equity) {
		this.equity = equity;
	}
	public void setWin(double win) {
		this.win = win;
	}
	public void setTie(double tie) {
		this.tie = tie;
	}
	public void setPotsWon(int potsWon) {
		this.potsWon = potsWon;
	}
	public void setPotsTied(int potsTied) {
		this.potsTied = potsTied;
	}
	public void setHand(String hand) {
		this.hand = hand;
	}
	public long getCurrentGameScore() {
		return currentGameScore;
	}
	public void setCurrentGameScore(long currentGameScore) {
		this.currentGameScore = currentGameScore;
	}
	public int getTotalGames() {
		return totalGames;
	}
	public void setTotalGames(int totalGames) {
		this.totalGames = totalGames;
	}
	public void increaseTotalGame(){
		totalGames++;
	}
	public void increasePotsWon(){
		potsWon++;
		potsWonTied++;
	}
	public void addToPotsWonTied(double val){
		potsWonTied += val;
	}
	public void increasePotsTied(){
		potsTied++;
	}
	public double getPotsWonTied() {
		return potsWonTied;
	}
	public void setPotsWonTied(double potsWonTied) {
		this.potsWonTied = potsWonTied;
	}
}