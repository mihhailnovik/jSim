/**
 * @author m1
 */
package mi.poker.tests.calculation;

import org.junit.Test;

import mi.poker.calculation.RangeStrategy;
import mi.poker.tests.BaseTest;

public class BaseRangeStrategyTest extends BaseTest {

	@Test
	public void testRangeStrategy(){
		assertEquals(RangeStrategy.getDefaultRangeStrategy().getRange("JJ+").length, 34);
	}
}
