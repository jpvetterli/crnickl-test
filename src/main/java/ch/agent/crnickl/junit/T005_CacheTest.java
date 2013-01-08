package ch.agent.crnickl.junit;

import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdateEvent;
import ch.agent.crnickl.api.UpdateEventSubscriber;
import ch.agent.crnickl.impl.DatabaseBackend;

public class T005_CacheTest extends AbstractTest {

	private static Boolean DUMP = false;
	private static Database db;
	private static boolean notified = false;
	
	@Override
	protected void firstSetUp() throws Exception {
		db = getContext().getDatabase();
		db.rollback(); // avoid logging events from previous test(s)
		((DatabaseBackend) db).setStrictNameSpaceMode(false);
		db.getUpdateEventPublisher().subscribe(new UpdateEventSubscriber() {
			@Override
			public void notify(UpdateEvent event) {
				notified = true;
				if (DUMP)
					System.err.println("T005* " + event.toString());
			}
		}, DBObjectType.CHRONICLE, true);
		Util.deleteChronicles(db, "bt.test");
	}

	@Override
	protected void lastTearDown() throws Exception {
		db.getUpdateEventPublisher().unsubscribeAll();
		((DatabaseBackend) db).setStrictNameSpaceMode(true);
		if (!notified)
			fail("no notification seen");
	}

	/**
	 * Delete and recreate same chronicle. A design issue caused the old
	 * chronicle to remain in the cache. This was bad from a logic point of view
	 * but was good for event logging, which relied on objects existing, except
	 * when deleted. So test1 exercised two distinct but mutually exclusive bugs.
	 */
	public void test1() {
		try {
			UpdatableChronicle uc = db.getTopChronicle().edit().createChronicle("test", false, "testing...", null, null);
			uc.applyUpdates();
			String su1 = uc.getSurrogate().toString();
			// access with "test" okay, because of non-strict mode
			String su2 = db.getChronicle("test", true).getSurrogate().toString();
			assertEquals(su1, su2);
			uc.destroy();
			uc.applyUpdates();
			uc = db.getTopChronicle().edit().createChronicle("test", false, "testing...", null, null);
			uc.applyUpdates();
			// DON'T commit... old version of system removed things from cache on commit
			String su3 = uc.getSurrogate().toString();
			String su4 = db.getChronicle("test", true).getSurrogate().toString();
			assertEquals(su3, su4);
			db.commit();
		} catch (Exception e) {
//			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			
		}
	}

}
