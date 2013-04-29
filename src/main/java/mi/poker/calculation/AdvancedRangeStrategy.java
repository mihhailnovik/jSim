package mi.poker.calculation;

import mi.poker.common.model.testbed.klaatu.CardSet;
import mi.poker.common.model.testbed.klaatu.Rank;
/**
 * @author Laurent
 */
public class AdvancedRangeStrategy extends RangeStrategy {
	@Override
	public CardSet[] getRange(String card) {
            //AJs+ ou KTo+ ou AJ+ ou 99+
            if (card.endsWith("+")) {
                return getRangePlus(card.substring(0, card.length()-1));
            }
            //A7-AT or A7s-ATs
            if (card.indexOf('-')!=-1) {
                return getRangeMinus(card);
            }
            return null;
	}

    private CardSet[] getRangePlus(String card) {
        
        Rank r1 = Rank.fromChar(card.charAt(0));
        Rank r2 = Rank.fromChar(card.charAt(1));
        String suit = new String(card.substring(2, 3));
        Rank highestRank;
        String highestString;
        if (r1.ordinal() >= r2.ordinal()){
            highestRank = Rank.fromChar(r1.toChar());
        } else {
            highestRank = Rank.fromChar(r2.toChar());
            r2 = r1;
        }
        highestString = String.valueOf(highestRank.toChar());
        Rank[] secondPossibleCardArray = new Rank[highestRank.pipValue()-r2.pipValue()];
        CardSet[] result = new CardSet[4*(highestRank.pipValue()-r2.pipValue())];
        for (Rank r : Rank.values()) {
            if (r.ordinal() < highestRank.ordinal() & r.ordinal() >= r2.ordinal()) {
                String s = String.valueOf(r.toChar());
                card = new StringBuilder(highestString).append(s).append(suit).toString();
                CardSet[] cs = HandParser.parsePossibleHands(card);
                System.arraycopy(cs, 0, result, 0, cs.length);
        }

        
                }

		return result;
    }

    private CardSet[] getRangeMinus(String card) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
