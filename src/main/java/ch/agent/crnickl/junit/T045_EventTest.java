package ch.agent.crnickl.junit;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.DBObjectType;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdateEvent;
import ch.agent.crnickl.api.UpdateEventSubscriber;

public class T045_EventTest extends AbstractTest {
	
	private class EventTester implements UpdateEventSubscriber {
		private UpdateEvent event;
		@Override
		public void notify(UpdateEvent event) {
			this.event = event;
		}
		public UpdateEvent getEvent() {
			return event;
		}
	}
	
	private static final String FULLNAME = "bt.entity";
	private static final String SIMPLENAME = "entity";
	private static Database db;
	private static EventTester tester;
	
	@Override
	protected void firstSetUp() throws Exception {
		db = getContext().getDatabase();
		tester = new EventTester();
		db.getUpdateEventPublisher().subscribe(tester, DBObjectType.CHRONICLE, false);
	}
	
	@Override
	protected void lastTearDown() throws Exception {
		Util.deleteChronicles(db, FULLNAME);
	}

	public void test001() {
		try {
			UpdatableChronicle e = db.getTopChronicle().edit().createChronicle(SIMPLENAME, false, "junit test 001", null, null);
			assertEquals(FULLNAME, e.getName(true));
			e.applyUpdates();
			db.commit();
			assertEquals(FULLNAME, ((Chronicle) tester.getEvent().getSourceOrNull()).getName(true));
		} catch (T2DBException e) {
			fail(e.toString());
		}
	}
	
	public void test002() {
		try {
			UpdatableChronicle e = db.getChronicle(FULLNAME, true).edit();
			e.destroy();
			e.applyUpdates();
			db.commit();
			assertEquals(db.getNamingPolicy().joinValueAndDescription(FULLNAME, "junit test 001"), tester.getEvent().getComment());
		} catch (Exception e) {
			fail(e.toString());
		}
	}


}

