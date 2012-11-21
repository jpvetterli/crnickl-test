package ch.agent.crnickl.junit;

import ch.agent.crnickl.api.Database;

public class T001_SetUpTest extends AbstractTest {

	public void test_setUp() {
		try {
			Database db = getContext().getDatabase();
			System.out.println(db.toString());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
