package ch.agent.crnickl.junit;

import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;

public class T050_ChronicleTest extends AbstractTest {

	private static Database db;
	private static final String FULLNAME = "bt.entitytest";
	private static final String FULLNAME_UPDATED = "bt.entitytest2";
	private static final String SIMPLENAME = "entitytest";
	private static final String SIMPLENAME_UPDATED = "entitytest2";
	
	@Override
	protected void firstSetUp() throws Exception {
		db = getContext().getDatabase();
		Chronicle testData = db.getChronicle(FULLNAME, false);
		if (testData != null) {
			Util.deleteChronicleCollection(testData);
			UpdatableChronicle upd = testData.edit();
			upd.destroy();
			upd.applyUpdates();
			db.commit();
		}
	}
	
	@Override
	protected void lastTearDown() throws Exception {
		Util.deleteChronicles(db, FULLNAME, FULLNAME_UPDATED);
	}

	public void test1() {
		try {
			UpdatableChronicle testEntity = ((UpdatableChronicle)db.getTopChronicle()).createChronicle(SIMPLENAME, false, "test", null, null);
			testEntity.applyUpdates();
			assertEquals(FULLNAME, testEntity.getName(true));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test2() {
		try {
			UpdatableChronicle testEntity = ((UpdatableChronicle)db.getTopChronicle()).createChronicle(SIMPLENAME, false, "test", null, null);
			testEntity.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, D.D40126);
		}
	}
	
	public void test3() {
		try {
			UpdatableChronicle testEntity = db.getChronicle(FULLNAME, true).edit();
			testEntity.destroy();
			testEntity.applyUpdates();
			assertFalse(testEntity.getSurrogate().getObject().isValid());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test4() {
		// original bug: NPE when getting non-existing attribute of entity in construction 
		try {
			UpdatableChronicle e = db.getTopChronicle().edit().createChronicle(SIMPLENAME, false, "junit test 001", null, null);
			Attribute<?> a = e.getAttribute("foo", false);
			assertNull(a);
			a = e.getAttribute("bar", true);
			expectException();
		} catch (Exception e) {
			assertException(e, D.D40114);
		}
	}
	
	public void test5() {
		try {
			UpdatableChronicle e = db.getTopChronicle().edit().createChronicle(SIMPLENAME, false, "junit test 001", null, null);
			e.applyUpdates();
			Attribute<?> a = e.getAttribute("foo", false);
			assertNull(a);
			a = e.getAttribute("bar", true);
			expectException();
		} catch (Exception e) {
			assertException(e, D.D40114);
		}
	}
	
	public void test6() {
		// original bug: NPE when creating non-existing attribute of entity in construction 
		try {
			UpdatableChronicle e = db.getChronicle(FULLNAME, true).edit();
			e.createSeries("foo");
			expectException();
		} catch (Exception e) {
			assertException(e, D.D40114);
		}
	}
	
	public void testUpdateChronicleName() {
		try {
			UpdatableChronicle chron = db.getChronicle(FULLNAME, true).edit();
			chron.setName(SIMPLENAME_UPDATED);
			chron.applyUpdates();
			assertEquals(SIMPLENAME_UPDATED,  db.getChronicle(FULLNAME_UPDATED, true).getName(false));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testUpdateChronicleNameToNull() {
		try {
			UpdatableChronicle chron = db.getChronicle(FULLNAME_UPDATED, true).edit();
			chron.setName(null);
			chron.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, D.D01103);
		}
	}
	
	public void testUpdateChronicleNameToIllegal() {
		try {
			UpdatableChronicle chron = db.getChronicle(FULLNAME_UPDATED, true).edit();
			chron.setName("!@#");
			chron.applyUpdates();
			System.out.println(chron.getName(true));
			expectException();
		} catch (Exception e) {
			assertException(e, D.D01104);
		}
	}
	
	public void testUpdateChronicleNameToEmpty() {
		try {
			UpdatableChronicle chron = db.getChronicle(FULLNAME_UPDATED, true).edit();
			chron.setName("");
			chron.applyUpdates();
			System.out.println(chron.getName(true));
			expectException();
		} catch (Exception e) {
			assertException(e, D.D01103);
		}
	}
	
	public void testUpdateChronicleDescription() {
		try {
			UpdatableChronicle chron = db.getChronicle(FULLNAME_UPDATED, true).edit();
			chron.setDescription("anything");
			chron.applyUpdates();
			assertEquals("anything",  db.getChronicle(FULLNAME_UPDATED, true).getDescription(false));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}