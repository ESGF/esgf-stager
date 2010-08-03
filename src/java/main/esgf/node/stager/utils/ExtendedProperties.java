package esgf.node.stager.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import esgf.node.stager.io.StagerException;

/**
 * Extends the normal property class to allow:
 * <ul>
 * <li>retrieve checked property (throws exception if not checked)</li>
 * <li>retrieve property casting it to the type of the passed default value if
 * any. (Only for Integer, Long, Double, Float and Boolean)</br> For example:
 * 
 * <pre>
 * int port = eProperties.getCheckedProperty(&quot;port&quot;, 22);
 * </pre>
 * 
 * (the int Integer conversion works because of unboxing, the method returns an
 * object.</li>
 * <li>retrieve property or default value if property not found</li>
 * <li>Read properties at filename (utf-8 only)</li>
 * <li>Write properties at filename (utf-8)</li>
 * </ul>
 * 
 * @author Estanislao Gonzalez
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
	 * @param properties properties being copied
	 */
	public ExtendedProperties(Properties properties) {
		super();
		this.putAll(properties);
		
		if (LOG.isDebugEnabled()) dumpProps();
	}
	
	/**
	 * Create an ExtendedProperties object from the given Properties one, but
	 * only importing properties which contain a definite prefix.
	 * 
	 * @param properties properties being copied
	 * @param prefix String to which all imported properties will match.
	 */
	public ExtendedProperties(String prefix, Properties properties) {
		super();
		for (Entry<Object, Object> e : properties.entrySet()) {
			
			if (((String)e.getKey()).startsWith(prefix)) {
				setProperty((String)e.getKey(), (String)e.getValue());
			}
		}
		if (LOG.isDebugEnabled()) dumpProps();
	}
	
	/**
	 * Load properties from given file (only UTF-8 supported)
	 * 
	 * @param fileName file to load properties from
	 * @throws IOException Cannot access file
	 * @throws FileNotFoundException File not found
	 */
	public ExtendedProperties(String fileName) throws FileNotFoundException,
			IOException {
		super();

		readFromFile(fileName);

		if (LOG.isDebugEnabled()) dumpProps("Properties read from file: "
				+ fileName + "\n");
	}
	
	private void readFromFile(String fileName) throws IOException {
		InputStreamReader reader = new InputStreamReader(new FileInputStream(
				fileName), "UTF-8");
		load(reader);
		reader.close();
	}
	
	private void writeToFile(String fileName) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
				fileName), "UTF-8");
		store(out, "Created by " + this.getClass().getCanonicalName() + " @ "
				+ String.format("%tF%n", Calendar.getInstance()));
		InputStreamReader reader = new InputStreamReader(new FileInputStream(
				fileName), "UTF-8");
		load(reader);
		reader.close();
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
	 * Write current properties to a file.
	 * 
	 * <pre>
	 * read(file1);
	 * write(file2);
	 * </pre>
	 * 
	 * does not imply file1 == file2 because the order is arbitrary and no
	 * comments from file1 were read.
	 * 
	 * @param fileName file to write properties to
	 * @throws IOException Cannot access file
	 */
	public void write(String fileName) throws IOException {
		writeToFile(fileName);

		if (LOG.isDebugEnabled()) LOG.debug("Properties saved to file:"
				+ fileName);
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
				LOG.error("Missing config property " + name);
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
