package ch.agent.crnickl.junit;

import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.impl.DatabaseBackend;

/**
 * Standalone_EntityTest tests that the name space can be left out in non-strict
 * mode: <code>dbStrictNameSpace=false</code>.
 */
public class T006_ChronicleTest_NonStrictMode extends AbstractTest {

	private Database db;
	private static boolean clean;
	private static final String FULLNAME = "bt.standalonetest";
	private static final String SIMPLENAME = "standalonetest";
	
	@Override
	protected void tearDown() throws Exception {
		((DatabaseBackend) db).setStrictNameSpaceMode(true);
		super.tearDown();
	}

	@Override
	protected void setUp() throws Exception {
		db = getContext().getDatabase();
		((DatabaseBackend) db).setStrictNameSpaceMode(false);
		if (!clean) {
			Chronicle testData = db.getChronicle(SIMPLENAME, false);
			if (testData != null) {
				Util.deleteChronicleCollection(testData);
				UpdatableChronicle upd = testData.edit();
				upd.destroy();
				upd.applyUpdates();
			}
			UpdatableChronicle ex = db.getTopChronicle().edit().createChronicle(SIMPLENAME, false, "standalone test", null, null);
			ex.applyUpdates();
			clean = true;
		}
	}
	
	public void test1() {
		assertFalse(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			UpdatableChronicle e = db.getChronicle(SIMPLENAME, true).edit();
			UpdatableChronicle ex = e.createChronicle("x", false, "it's x", null, null);
			ex.applyUpdates();
			Chronicle ent = db.getChronicle(SIMPLENAME + ".x", true);
			assertEquals(SIMPLENAME + ".x", ent.getName(true));
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test2() {
		assertFalse(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			Chronicle en = db.getChronicle(SIMPLENAME + ".x", true);
			assertEquals(SIMPLENAME + ".x", en.getName(true));
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test3() {
		assertFalse(((DatabaseBackend) db).isStrictNameSpaceMode());
		try {
			// full name tolerated in non-strict mode
			Chronicle en = db.getChronicle(FULLNAME + ".x", true);
			assertEquals(SIMPLENAME + ".x", en.getName(true));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	
}