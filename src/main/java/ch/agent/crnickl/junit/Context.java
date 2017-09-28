package ch.agent.crnickl.junit;

import java.net.URL;
import java.util.Map;

import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.SimpleDatabaseManager;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;

public class Context {

	private SimpleDatabaseManager dbm;
	private boolean isTransactional = false;
	private boolean supportsRegex = false;
	private boolean isPersistent = false;
	
	/**
	 * The database configuration file must be accessible on the classpath
	 * with the name defined in the variable DB_CONFIG.
	 */
	private static final String DB_CONFIG = "db.test.config";
	private static final String IS_PERSISTENT = "feature.isPersistent";
	private static final String IS_TRANSACTIONAL = "feature.isTransactional";
	private static final String SUPPORTS_REGEX = "feature.supportsRegex";
	private static final String TRUE = "true";
	
	protected Database setup() throws Exception {
		if (dbm == null) {
			URL url = ClassLoader.getSystemClassLoader().getResource(DB_CONFIG);
			if (url == null)
				throw new RuntimeException(String.format(
						"Resource \"%s\" not found.", DB_CONFIG));
			dbm = new SimpleDatabaseManager("file=" + url.getFile());
			
			Map<String, String> parameters = dbm.getParameters();
			isTransactional = TRUE.equals(parameters.get(IS_TRANSACTIONAL));
			supportsRegex = TRUE.equals(parameters.get(SUPPORTS_REGEX));
			isPersistent = TRUE.equals(parameters.get(IS_PERSISTENT));
			
			setup(dbm, parameters);
		}
		return dbm.getDatabase();
	}
	
	/**
	 * Override this method for special needs. Typically, the method is used for
	 * running the DDL to define the schema of the database application. By
	 * default, the method executes {@link #setUpNumberType}, assuming the
	 * schema has already been defined.
	 * 
	 * @param dbm
	 *            the database manager
	 * @param parameters
	 *            configuration parameter
	 * 
	 * @throws Exception on failure
	 */
	protected void setup(SimpleDatabaseManager dbm, Map<String, String> parameters) throws Exception {
		setUpNumberType(dbm.getDatabase());
	}
	
	public Database getDatabase() throws Exception {
		if (dbm == null)
			setup();
		return dbm.getDatabase();
	}
	
	public boolean isTransactional() {
		return isTransactional;
	}
	
	public boolean hasStandardRegex() {
		return supportsRegex;
	}
	
	public boolean isPersistent() {
		return isPersistent;
	}
	
	protected void setUpNumberType(Database db)  throws Exception {
		try {
			db.getValueType("numeric").typeCheck(Double.class);
		} catch (Exception e) {
			UpdatableValueType<String> uvt = db.createValueType("numeric", false, "NUMBER");
			uvt.applyUpdates();
			@SuppressWarnings("rawtypes")
			UpdatableValueType<ValueType> uvtvt = db.getTypeBuiltInProperty().getValueType().typeCheck(ValueType.class).edit();
			uvtvt.addValue(uvtvt.getScanner().scan("numeric"), null);
			uvtvt.applyUpdates();
			db.commit();
		}
	}
	
}
