package ch.agent.crnickl.junit;

import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import ch.agent.crnickl.T2DBMsg.D;
import ch.agent.crnickl.T2DBMsg.E;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableProperty;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.time.Day;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class T015_SchemaChronicleSeriesValueTest extends AbstractTest {

	private static Database db;
	
	@Override
	protected void firstSetUp() throws Exception {
		db = getContext().getDatabase();
	}

	@Override
	protected void lastTearDown() throws Exception {
		// the sequence is important
		Util.deleteChronicles(db, "bt.fooent");
		Util.deleteSchema(db, "foo schema");
		Util.deleteProperties(db, "foo property");
		Util.deleteValueTypes(db, "foo type");
	}

	public void test012_create_type() {
		try {
			UpdatableValueType<String> vt = db.createValueType("foo type", true, "TEXT");
			vt.addValue(vt.getScanner().scan("bar"), "it's bar");
			vt.applyUpdates();
			assertEquals("foo type", db.getValueType(vt.getSurrogate()).getName());
			assertEquals("it's bar", db.getValueType(vt.getSurrogate()).getValueDescriptions().get("bar"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test013_value_type() {
		try {
			ValueType<String> vt = db.getValueType("foo type");
			Collection<String> values = vt.getValues(null);
			assertEquals("bar - it's bar", values.iterator().next());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test016_add_value_but_should_not() {
		try {
			UpdatableValueType<String> vt = db.getValueType("foo type").typeCheck(String.class).edit();
			vt.updateValue(vt.getScanner().scan("baz"), "BAZ");
			vt.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, D.D10123);
		}
	}
	
	public void test020_add_value_and_delete_it() {
		try {
			UpdatableValueType<String> vt = db.getValueType("foo type").typeCheck(String.class).edit();
			vt.addValue(vt.getScanner().scan("baz"), "BAZ");
			vt.addValue(vt.getScanner().scan("barf"), "BARF");
			vt.applyUpdates();
			vt.deleteValue(vt.getScanner().scan("baz"));
			vt.applyUpdates();
			assertTrue(vt.getValueDescriptions().get("baz")== null);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void test024_create_property() {
		try {
			ValueType<String> type = db.getValueType("foo type");
			UpdatableProperty<String> p = db.createProperty("foo property", type, true);
			p.applyUpdates();
			assertEquals("foo type", db.getProperty(p.getSurrogate()).getValueType().getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test026_delete_value_type_in_use() {
		try {
			UpdatableValueType<String> type = db.getValueType("foo type").typeCheck(String.class).edit();
			type.destroy();
			type.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10145, E.E10149);
		}
	}
	
	public void test028_create_schema() {
		try {
			UpdatableSchema schema = db.createSchema("foo schema", null);
			schema.addAttribute(2);
			schema.setAttributeProperty(2, db.getProperty("foo property", true));
			schema.setAttributeDefault(2, "bar");
			schema.addSeries(1);
			schema.setSeriesName(1, "fooser");
			schema.setSeriesType(1, "numeric");
			schema.setSeriesTimeDomain(1, Day.DOMAIN);
			schema.applyUpdates();
			assertEquals("foo property", db.getSchemas("foo schema").iterator().next().
					getAttributeDefinition(2, true).getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test032_create_entity() {
		try {
			Schema schema = db.getSchemas("foo schema").iterator().next();
			UpdatableChronicle ent = db.getTopChronicle().edit().createChronicle("fooent", false, "foo entity", null, schema);
			ent.getAttribute("foo property", true);
			ent.applyUpdates();
			assertNotNull(ent.getAttribute("foo property", false));
			assertNull(ent.getAttribute("bar property", false));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void test036_create_series_and_add_value() {
		try {
			UpdatableChronicle ent = db.getChronicle("bt.fooent", true).edit();
			UpdatableSeries<Double> s = ent.updateSeries("fooser");
			assertNull(s);
			s = db.getUpdatableSeries("bt.fooent.fooser", true);
			assertNotNull(s);
			s.setValue(Day.DOMAIN.time("2011-06-30"), 42.);
			s.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test040_create_series_and_delete_value() {
		try {
			UpdatableChronicle ent = db.getChronicle("bt.fooent", true).edit();
			UpdatableSeries<Double> s = ent.updateSeries("fooser");
			assertNotNull(s);
			s.setValue(Day.DOMAIN.time("2011-06-30"), Double.NaN);
			s.applyUpdates();
			// next should not throw an exception
			s.setValue(Day.DOMAIN.time("2011-06-30"), Double.NaN);
			s.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test044_update_attribute() {
		try {
			UpdatableChronicle ent = db.getChronicle("bt.fooent", true).edit();
			Attribute<String> attr = ent.getAttribute("foo property", true).typeCheck(String.class);
			assertEquals("bar", attr.get());
			attr.set("baz");
			expectException();
		} catch (Exception e) {
			assertException(e, D.D20110, D.D10115);
		}
	}
	
	public void test048_update_attribute() {
		try {
			UpdatableProperty<String> prop = db.getProperty("foo property", true).typeCheck(String.class).edit();
			UpdatableValueType<String> vt = prop.getValueType().edit();
			vt.addValue("baz", "this is baz");
			vt.applyUpdates();
			UpdatableChronicle ent = db.getChronicle("bt.fooent", true).edit();
			Attribute<String> attr = ent.getAttribute("foo property", true).typeCheck(String.class);
			attr.set("baz");
			ent.setAttribute(attr);
			ent.applyUpdates();
			Attribute<String> a = ent.getAttribute("foo property", true).typeCheck(String.class);
			assertEquals("baz", a.get());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test049_update_attribute() {
		try {
			UpdatableChronicle ent = db.getChronicle("bt.fooent", true).edit();
			Attribute<String> attr = ent.getAttribute("foo property", true).typeCheck(String.class);
			assertEquals("baz", attr.get());
			attr.set("barf");
			ent.setAttribute(attr);
			ent.applyUpdates();
			attr.set("baz");
			ent.setAttribute(attr);
			ent.applyUpdates();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test050_delete_value_type_value_in_use() {
		try {
			UpdatableValueType<String> type = db.getValueType("foo type").typeCheck(String.class).edit();
			type.deleteValue("baz");
			type.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10146, E.E10158);
		}
	}
	
	public void test051_delete_value_type_value_used_as_default() {
		try {
			UpdatableValueType<String> type = db.getValueType("foo type").typeCheck(String.class).edit();
			type.deleteValue("bar");
			type.applyUpdates();
			expectException();
		} catch (Exception e) {
			assertException(e, E.E10146, E.E10157);
		}
	}

	public void test052_get_attribute() {
		try {
			Chronicle ent = db.getChronicle("bt.fooent", true);
			Attribute<String> attr = ent.getAttribute("foo property", true).typeCheck(String.class);
			assertEquals("baz", attr.get());
			assertEquals("this is baz", attr.getDescription(true));
			assertEquals(null, attr.getDescription(false));
			assertEquals("this is baz", attr.getProperty().getValueType().getValueDescriptions().get("baz"));
			AttributeDefinition<String> def = ent.getSchema(true).getAttributeDefinition("foo property", true).typeCheck(String.class);
			assertEquals("bar", def.getAttribute().get());
			assertEquals("it's bar", def.getAttribute().getDescription(true));
			assertEquals(null, def.getAttribute().getDescription(false));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}	
	
}
