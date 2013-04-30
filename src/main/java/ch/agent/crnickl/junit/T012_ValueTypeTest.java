package ch.agent.crnickl.junit;

import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.T2DBMsg;
import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.T2DBMsg.E;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Surrogate;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueScanner;
import ch.agent.crnickl.api.ValueType;

/**
 * These tests must be executed together. They build upon each other. 
 * The sequence is important. The last tests cleanup.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class T012_ValueTypeTest extends AbstractTest {

	private Database db;
	
	@Override
	protected void setUp() throws Exception {
		db = getContext().getDatabase();
	}

	public void test_010_create_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("foo-type", true, "TEXT");
			vt.addValue(vt.getScanner().scan("bar"), "it's bar");
			vt.addValue(vt.getScanner().scan("baz"), "it's baz");
			vt.applyUpdates();
			assertEquals("foo-type", db.getValueType(vt.getSurrogate()).getName());
			assertEquals("it's bar", db.getValueType(vt.getSurrogate()).getValueDescriptions().get("bar"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_020_create_another_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("bar-type", false, "TEXT");
			vt.applyUpdates();
			assertEquals(2, db.getValueTypes("*-type").size());
			if (getContext().hasStandardRegex())
				assertEquals(2, db.getValueTypes("/.*-t[xyz]pe/").size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_030_rename_type() {
		try {
			ValueType<String> vt = db.getValueType("foo-type");
			Surrogate s = vt.getSurrogate();
			UpdatableValueType<String> uvt = vt.edit();
			uvt.setName("moo-type");
			uvt.applyUpdates();
			assertEquals("moo-type", db.getValueType(s).getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private void helper_delete_type(String name) throws Exception {
		ValueType<String> vt = db.getValueType(name);
		UpdatableValueType<String> uvt = vt.edit();
		uvt.destroy();
		uvt.applyUpdates();
		try {
			db.getValueType(name);
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10109);
		}
	}
	
	public void test_040_delete_types_by_pattern() {
		try {
			Collection<ValueType<?>> vts = db.getValueTypes("*-type");
			for (ValueType<?> vt : vts) {
				helper_delete_type(vt.getName());
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_050_delete_non_existing_type() {
		try {
			helper_delete_type("foo");
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10109);
		}
	}
	
	public void test_060_recreate_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("foo", true, "TEXT");
			vt.applyUpdates();
			assertEquals(0, vt.getValues().size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void test_070_type_add_value() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.addValue(vt.getScanner().scan("foo1"), "Foo 1");
			vt.applyUpdates();
			assertEquals(1, vt.getValues().size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_080_add_more_values() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.addValue(vt.getScanner().scan("foo2"), "Foo 2");
			vt.addValue(vt.getScanner().scan("foo4"), "Foo 4");
			vt.applyUpdates();
			assertEquals(3, vt.getValues().size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_090_get_value_and_description() {
		try {
			ValueType<String> vt = db.getValueType("foo");
			Collection<String> values = vt.getValues(null);
			assertEquals("foo1 - Foo 1", values.iterator().next());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void test_100_get_all_values() {
		try {
			ValueType<String> vt = db.getValueType("foo").typeCheck(String.class).edit();
			assertTrue(vt.getValueDescriptions().get("foo1").equals("Foo 1"));
			assertTrue(vt.getValueDescriptions().get("baz") == null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_110_update_type_no_update() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_120_update_existing_value() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.updateValue(vt.getScanner().scan("foo1"), "Foo 1 edited");
			vt.applyUpdates();
			assertEquals("Foo 1 edited", vt.getValueDescriptions().get("foo1"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test_130_update_non_existing_value() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.updateValue(vt.getScanner().scan("foo3"), "Foo 3");
			vt.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, D.D10123);
		}
	}
	
	public void test_140_delete_non_existing_value() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.deleteValue(vt.getScanner().scan("foo3"));
			vt.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, D.D10122);
		}
	}
	
	public void test_150_delete_existing_value() {
		try {
			UpdatableValueType<String> vt = 
					db.getValueType("foo").typeCheck(String.class).edit();
			vt.deleteValue(vt.getScanner().scan("foo4"));
			vt.applyUpdates();
			assertEquals(2, vt.getValues().size());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void test_160_delete_existing_type() {
		try {
			helper_delete_type("foo");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public static class Foo {
		private String foo1;
		private int foo2;
		
		public Foo(String foo1, int foo2) {
			this.foo1 = foo1;
			this.foo2 = foo2;
		}

		public String getFoo1() {
			return foo1;
		}

		public int getFoo2() {
			return foo2;
		}

		@Override
		public String toString() {
			return foo1 + ":" + foo2;
		}
		
	}
	
	public static class FooScanner implements ValueScanner<Foo> {

		@Override
		public Class<Foo> getType() {
			return Foo.class;
		}

		@Override
		public Foo scan(String value) throws T2DBException {
			try {
				String[] parts = value.split(":");
				if (parts.length == 2)
					return new Foo(parts[0], Integer.valueOf(parts[1]));
			} catch(Exception e) {
				throw T2DBMsg.exception(e, "not a Foo: " + value);
			}
			throw T2DBMsg.exception("not a Foo: " + value);
		}

		@Override
		public void check(Foo value) throws T2DBException {
		}

		@Override
		public String toString(Foo value) throws T2DBException {
			return value.toString();
		}
		
	}

	public void test_170_create_custom_type() {
		try {
			UpdatableValueType<Foo> vt = db.createValueType("foo", true, FooScanner.class.getName());
			vt.addValue(vt.getScanner().scan("bar:1"), "it's bar:1");
			try {
				vt.addValue(vt.getScanner().scan("baz"), "it's baz:2");
				expectException();
			} catch (T2DBException e) {
				assertTrue(e.getMessage().startsWith("not a Foo"));
			}
			vt.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	public void test_180_delete_custom_type() {
		try {
			UpdatableValueType<Foo> vt = db.getValueType("foo").typeCheck(Foo.class).edit();
			vt.destroy();
			vt.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
