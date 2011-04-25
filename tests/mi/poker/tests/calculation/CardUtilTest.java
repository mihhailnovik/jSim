package mi.poker.tests.calculation;

import org.junit.Test;

import mi.poker.calculation.CardUtil;
import mi.poker.common.model.testbed.klaatu.Card;
import mi.poker.tests.BaseTest;

public class CardUtilTest extends BaseTest{

	@Test
	public void testBuildCard(){
		assertTrue(CardUtil.buildCard('T', 's').equals(new Card("Ts")));
	}
}
