package ch.agent.crnickl.junit;

import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.T2DBMsg.E;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;

/**
 * These tests must be executed together. They build upon each other. 
 * The sequence is important. The last tests cleanup.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class T013_PropertyTest extends AbstractTest {

	private Database db;
	private static boolean DUMP = false;
	
	@Override
	protected void setUp() throws Exception {
		db = getContext().getDatabase();
	}

	public void test_010_create_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("foo type", true, "TEXT");
			vt.addValue(vt.getScanner().scan("bar"), "it's bar");
			vt.addValue(vt.getScanner().scan("baf"), "it's baf");
			vt.applyUpdates();
			assertEquals("foo type", db.getValueType(vt.getSurrogate()).getName());
			assertEquals("it's bar", db.getValueType(vt.getSurrogate()).getValueDescriptions().get("bar"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_020_create_another_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("bar type", true, "TEXT");
			vt.addValue(vt.getScanner().scan("foo"), "it's foo");
			vt.addValue(vt.getScanner().scan("baf"), "it's baf");
			vt.applyUpdates();
			assertEquals("bar type", db.getValueType(vt.getSurrogate()).getName());
			assertEquals("it's foo", db.getValueType(vt.getSurrogate()).getValueDescriptions().get("foo"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_030_create_property() {
		try {
			ValueType<String> type = db.getValueType("foo type");
			UpdatableProperty<String> p = db.createProperty("foo property", type, true);
			p.applyUpdates();
			assertEquals("foo type", db.getProperty(p.getSurrogate()).getValueType().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_040_create_another_property() {
		try {
			ValueType<String> type = db.getValueType("bar type");
			UpdatableProperty<String> p = db.createProperty("bar property", type, true);
			p.applyUpdates();
			assertEquals("bar type", db.getProperty(p.getSurrogate()).getValueType().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_050_cannot_create_existing_property() {
		try {
			ValueType<String> type = db.getValueType("bar type");
			UpdatableProperty<String> p = db.createProperty("bar property", type, true);
			p.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, E.E20114);
		}
	}
	
	public void test_060_property_detects_bad_value() {
		try {
			UpdatableProperty<String> p = db.getProperty("foo property", true).typeCheck(String.class).edit();
			p.scan("baz");
			expectException();
		} catch (Exception e) {
			assertException(e, D.D20110);
		}
	}
	
	public void test_070_property_accepts_good_value() {
		try {
			UpdatableProperty<String> p = db.getProperty("foo property", true).typeCheck(String.class).edit();
			p.scan("baf");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void test_080_get_properties_by_pattern() {
		try {
			Collection<Property<?>> props = db.getProperties("*prop*");
			if (DUMP) {
				for (Property<?> prop : props) {
					System.err.println(prop.toString());
					ValueType<?> vt = prop.getValueType();
					System.err.println("  " + vt.toString());
					if (vt.isRestricted()) {
						for (String v : vt.getValues(null)) {
							System.err.println("    " + v);
						}
					}
				}
			}
			assertEquals(2, props.size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_090_get_all_properties() {
		try {
			Collection<Property<?>> props = db.getProperties(null);
			if (DUMP) {
				for (Property<?> prop : props) {
					System.err.println(prop.toString());
					ValueType<?> vt = prop.getValueType();
					System.err.println("  " + vt.toString());
					if (vt.isRestricted()) {
						for (String v : vt.getValues(null)) {
							System.err.println("    " + v);
						}
					}
				}
			}
			assertEquals(6, props.size()); // 2 + 4 built-in properties
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_100_rename_property() {
		try {
			UpdatableProperty<String> p = db.getProperty("foo property", true).typeCheck(String.class).edit();
			p.setName("moo property");
			p.applyUpdates();
			assertEquals("moo property", db.getProperty("moo property", true).getName());
			db.getProperty("foo property", true);
			expectException();
		} catch (Exception e) {
			assertException(e, D.D20109);
		}
	}
	
	public void test_110_cannot_delete_value_type_in_use() {
		try {
			UpdatableValueType<String> type = db.getValueType("foo type").typeCheck(String.class).edit();
			type.destroy();
			type.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10145, E.E10149);
		}
	}
	
	public void test_120_delete_property_and_type() {
		try {
			UpdatableProperty<String> p = db.getProperty("moo property", true).typeCheck(String.class).edit();
			UpdatableValueType<String> vt = p.getValueType().edit();
			p.destroy();
			p.applyUpdates();
			if (db.getProperty("moo property", false) != null)
				fail("foo property found");
			vt.destroy();
			vt.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_130_delete_other_property_and_type() {
		try {
			UpdatableProperty<String> p = db.getProperty("bar property", true).typeCheck(String.class).edit();
			UpdatableValueType<String> vt = p.getValueType().edit();
			p.destroy();
			p.applyUpdates();
			vt.destroy();
			vt.applyUpdates();
			if (db.getProperty("bar property", false) != null)
				fail("bar property found");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
