package esgf.node.stager.io;

import java.io.IOException;

/**
 * Signals that the access to the Stager has failed.
 * 
 * @author Estanislao Gonzalez
 */
public class StagerException extends IOException {
	private static final long serialVersionUID = 6150756387764528581L;

	/**
	 * Signal different Stager Exceptions. 
	 * @author Estanislao Gonzalez
	 */
	public enum Code {
		/** For everything not listed here. */
		UNDEFINED,
		/** This failure is unsolvable, e.g. remote host unreachable. */
		PERMANENT_FAILURE,
		/**
		 * The failure might be solvable. It can be used in case of an allocation
		 * error if the complete cache is being locked by downloading parties.
		 */
		TEMPORARY_FAILURE,
		/** If a requested file does not exists on the remote system. */
		FILE_NOT_FOUND
	}

	private final Code errorCode;

	/**
	 * Constructs the exception with a message.
	 * @param string cause description
	 */
	public StagerException(String string) {
		this(Code.UNDEFINED, string);
	}

	/**
	 * Constructs the exception with a message and an error code.
	 * @param error error code for the exception
	 * @param string cause description
	 */
	public StagerException(Code error, String string) {
		super(string);
		errorCode = error;
	}

	/**
	 * Constructs the exception with a message, an error code and the throwable
	 * that caused it.
	 * 
	 * @param error exceptions error code
	 * @param string cause description
	 * @param cause wrapped throwable which caused this exception
	 */
	public StagerException(Code error, String string, Throwable cause) {
		super(string, cause);
		errorCode = error;
	}

	/**
	 * Returns the error code.
	 * 
	 * @return the error code
	 */
	public Code getErrorCode() {
		return errorCode;
	}

}
