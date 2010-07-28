package esgf.node.stager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import esgf.node.stager.io.StagerException;

/**
 * Extends the normal property class to allow:
 * <ul>
 * <li>retrieve checked property (throws exception if not checked)</li>
 * </ul>
 * @author Estanislao Gonzalez
 *
 */
public class ExtendedProperties extends Properties {
	private static final long serialVersionUID = 6780295775257064354L;
	
	private static final Logger LOG = Logger.getLogger(ExtendedProperties.class);
	
	/**
	 * Creates an empty property object.
	 */
	public ExtendedProperties() {
		super();
	}

	/**
	 * Create an ExtendedProperties object from the given Properties one.
	 * @param defaults
	 */
	public ExtendedProperties(Properties defaults) {
		super(defaults);
		if (LOG.isDebugEnabled()) dumpProps();
	}
	
	/**
	 * Create an ExtendedProperties object from the given Properties one,
	 * but only importing properties which contain a definite prefix.
	 * @param defaults
	 * @param prefix String to which all imported properties will match.
	 */
	public ExtendedProperties(String prefix, Properties defaults) {
		super();
		for (Entry<Object, Object> e : defaults.entrySet()) {
			
			if (((String)e.getKey()).startsWith(prefix)) {
				setProperty((String)e.getKey(), (String)e.getValue());
			}
		}
		if (LOG.isDebugEnabled()) dumpProps();
	}
	
	/**
	 * Load properties from given file
	 * @param fileName file to load properties from
	 * @throws IOException Cannot access file
	 * @throws FileNotFoundException File not found
	 */
	public ExtendedProperties(String fileName) throws FileNotFoundException, IOException {
		super();
		
		load(new FileInputStream(new File(fileName)));
		if (LOG.isDebugEnabled()) dumpProps("File: " + fileName);
	}
	
	private void dumpProps(String... info) {
		StringBuilder sb = new StringBuilder();
		sb.append("Dumping properties\n");
		for (int i = 0; i < info.length; i++) {
			sb.append(info[i]);
		}
		sb.append("Total items: ").append(size()).append('\n');
		for (Entry<Object, Object> e : entrySet()) {
			sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
		}
		LOG.debug(sb.toString());
	}
	
	/**
	 * Gets a property or its default value if no property was found. If the
	 * property wasn't found and no default value was provided an exception will
	 * be thrown. This call is being logged, if you don't wan't the logg system
	 * to log it use {@link #getCheckedProperty(boolean, String, Object...)}
	 * instead.
	 * 
	 * @param name name of the property to retrieve
	 * @param defValue default value to return if property is missing (not
	 *            required)
	 * @return the property if present, defValue if the property wasn't found.
	 * @throws StagerException If both property and default value are missing.
	 */
	public <T> T getCheckedProperty(String name,
			T... defValue) throws StagerException {
		return getCheckedProperty(true, name, defValue);
	}
	
	/**
	 * * Gets a property or its default value if no property was found. If the
	 * property wasn't found and no default value was provided an exception will
	 * be thrown.
	 * 
	 * @param logFound if we should log the case where the parameter is found.
	 * @param name name of the property to retrieve
	 * @param defValue default value to return if property is missing (not
	 *            required)
	 * @return the property if present, defValue if the property wasn't found.
	 * @throws StagerException If both property and default value are missing.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCheckedProperty(boolean logFound, String name,
			T... defValue) throws StagerException {
		String tmp = getProperty(name);
		if (tmp == null) {
			if (defValue.length == 0) {
				throw new StagerException("Missing config property " + name);
			} else {
				LOG.warn(name+ " missing, using default value: " + defValue[0]);
				return defValue[0];
			}
		} else {
			if (logFound) LOG.info("Param '" + name + "' value: " + tmp);
			//if we have a default value AND a property try to perform  
			//some basic conversion to provide the expected object.
			if (defValue.length > 0) {
				if (defValue[0] instanceof Integer) {
					return (T)new Integer(tmp);
				} else if (defValue[0] instanceof Long) {
					return (T)new Long(tmp);
				} else if (defValue[0] instanceof Float) {
					return (T)new Float(tmp);
				} else if (defValue[0] instanceof Double) {
					return (T)new Double(tmp);
				} else if (defValue[0] instanceof Boolean) {
					return (T)new Boolean(tmp);
				}
			}
			
		}
		return (T)tmp;
	}
}
