package ch.agent.crnickl.junit;

public class T043_SparseSeriesTest extends T042_SeriesValuesTest {

	protected static String SCHEMA = "t043";
	
	@Override
	protected boolean isSparse() {
		return true;
	}

	
	
}