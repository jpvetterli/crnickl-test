package ch.agent.crnickl.junit;

public class T001_SetUpTest extends AbstractTest {

	@Override
	protected void setUp() throws Exception {
		getContext().getDatabase();
	}

	public void test_setUp() {
		assertEquals("setup okay", "setup okay");
	}

	
}
