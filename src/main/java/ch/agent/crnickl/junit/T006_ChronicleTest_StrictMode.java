package ch.agent.crnickl.junit;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.impl.DatabaseBackend;

/**
 * Standalone_EntityTest tests that the name space cannot be left out in strict
 * mode: <code>dbStrictNameSpace=true</code>.
 */
public class T006_ChronicleTest_StrictMode extends AbstractTest {

	private Database db;
	private static boolean clean;
	private static final String FULLNAME = "bt.standalonetest";
	private static final String SIMPLENAME = "standalonetest";
	
	@Override
	protected void setUp() throws Exception {
		db = getContext().getDatabase();
		if (!clean) {
			Chronicle testData = db.getChronicle(FULLNAME, false);
			if (testData != null) {
				Util.deleteChronicleCollection(testData);
				UpdatableChronicle upd = testData.edit();
				upd.destroy();
				upd.applyUpdates();
			}
			db.getTopChronicle().edit().createChronicle(SIMPLENAME, false, "standalone test", null, null).applyUpdates();
			clean = true;
		}
	}
	
	public void test1() {
		assertTrue(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			UpdatableChronicle e = db.getChronicle(FULLNAME, true).edit();
			UpdatableChronicle ex = e.createChronicle("x", false, "it's x", null, null);
			ex.applyUpdates();
			Chronicle ent = db.getChronicle(FULLNAME + ".x", true);
			assertEquals(FULLNAME + ".x", ent.getName(true));
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test2() {
		assertTrue(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			db.getChronicle(SIMPLENAME + ".x", true);
			expectException();
		} catch (Exception e) {
			assertException(e, D.D40103);
		}
	}
	
	public void test3() {
		assertTrue(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			Chronicle en = db.getChronicle(FULLNAME + ".x", true);
			assertEquals(FULLNAME + ".x", en.getName(true));
		} catch (T2DBException e) {
			fail(e.toString());
		}
	}

	
}