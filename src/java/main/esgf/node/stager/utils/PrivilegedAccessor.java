package esgf.node.stager.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Provides access to private fields, classes & methods. This should only be
 * used for testing and output routines that are not part of normal operation
 * and which might imply changing the visibility of the original
 * methods/attributes for this solely purpose. A minimal SecurityContext will
 * inhibit this class from working properly.
 * 
 * @author Estanislao Gonzalez
 */
public final class PrivilegedAccessor {
	private PrivilegedAccessor() {
	}

	private static Class<?>[] expandClasses(Object... o) {
		Class<?>[] classes = new Class<?>[o.length];
	
		for (int i = 0; i < classes.length; i++) {
			//bacause of auto-boxing!
			if (o[i] instanceof Integer) {
				classes[i] = Integer.TYPE;
			} else if (o[i] instanceof Long) {
				classes[i] = Long.TYPE;
			} else if (o[i] instanceof Float) {
				classes[i] = Float.TYPE;
			} else if (o[i] instanceof Double) {
				classes[i] = Double.TYPE;
			} else if (o[i] instanceof Character) {
				classes[i] = Character.TYPE;
			} else if (o[i] instanceof Boolean) {
				classes[i] = Boolean.TYPE;
			} else if (o[i] == null) {
				throw new IllegalArgumentException("Cannot imply type of a null variable");
			} else {
				classes[i] = o[i].getClass();
			}
		}
		
		return classes;
	}
	
	/**
	 * Returns a private field from an instance or class if it's declared
	 * static.
	 * 
	 * @param source object whose filed will be grabbed.
	 * @param fieldName name of the field
	 * @return the field object
	 */
	public static Object getField(Object source, String fieldName) {
		try {
			Class<?> sourceClass;
			if (source instanceof Class<?>) {
				sourceClass = (Class<?>) source;
			} else {
				sourceClass = source.getClass();
			}
			Field f = sourceClass.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Alters the value of a private field.
	 * 
	 * @param source object whose filed will be grabbed.
	 * @param fieldName name of the field
	 * @param value the value this field is going to have afterwards.
	 */
	public static void setField(Object source, String fieldName, Object value) {
		try {
			Class<?> sourceClass;
			if (source instanceof Class<?>) {
				sourceClass = (Class<?>) source;
			} else {
				sourceClass = source.getClass();
			}
			Field f = sourceClass.getDeclaredField(fieldName);
			f.setAccessible(true);
			if (Modifier.isFinal(f.getModifiers())) {
				// for final values we have to modify the field to can change
				// them
				// (don't think it will function always, but it does here ;-) )
				PrivilegedAccessor.setField(f, "modifiers", f.getModifiers()
						& (~Modifier.FINAL));
			}
			if (value instanceof Boolean) {
				f.setBoolean(source, (Boolean) value);
			} else if (value instanceof Integer) {
				f.setInt(source, (Integer) value);
			} else {
				// if more required add. If here we assume is a normal object
				f.set(source, value);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves an internal private class.
	 * 
	 * @param source object to get an internal class from.
	 * @param className name of the internal class
	 * @return the class object
	 */
	public static Class<?> getInternalClass(Object source, String className) {
		try {
			Class<?> sourceClass;
			if (source instanceof Class<?>) {
				sourceClass = (Class<?>) source;
			} else {
				sourceClass = source.getClass();
			}
			Class<?>[] classes = sourceClass.getDeclaredClasses();
			for (int i = 0; i < classes.length; i++) {
				// get the simple class name
				String name = classes[i].getName().substring(
						classes[i].getName().lastIndexOf('$') + 1);

				if (className.equals(name)) {
					return classes[i];
				}
			}
			throw new IllegalArgumentException("Class not found: " + className);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Instantiate a private internal class.
	 * @param sourceClass Class object to instantiate
	 * @param params parameters for the constructor
	 * @return a new instance.
	 */
	public static Object instantiate(Class<?> sourceClass, Object... params) {
		try {
			Class<?>[] classes = expandClasses(params);

			Constructor<?> c = sourceClass.getDeclaredConstructor(classes);
			c.setAccessible(true);
			return c.newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Call a private method.
	 * 
	 * @param source source object (might be a Class object)
	 * @param methodName name of method to invoke
	 * @param params parameters for the call (might be empty)
	 * @return the resulting Object or null if it was void (while returning a
	 *         primitive it will get encapsulated into an Object)
	 */
	public static Object callMethod(Object source, String methodName,
			Object... params) {
		try {
			Class<?> sourceClass;
			if (source instanceof Class<?>) {
				sourceClass = (Class<?>) source;
			} else {
				sourceClass = source.getClass();
			}
			Class<?>[] classes = expandClasses(params);
			
			Method m = sourceClass.getDeclaredMethod(methodName, classes);
			m.setAccessible(true);
			return m.invoke(source, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
