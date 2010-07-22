package esgf.node.stager.io;

import java.io.IOException;

/**
 * Signals that the access to the Stager has failed.
 * 
 * @author Estanislao Gonzalez
 */
public class StagerException extends IOException {
	private static final long serialVersionUID = 6150756387764528581L;

	public enum Code {
		/** For everything not listed here. */
		UNDEFINED,
		/** This fail is unsolvable, e.g. remote host unreachable. */
		PERMANENT_FAIL,
		/**
		 * The fail might be solvable. It can be used in case of an allocation
		 * error if the complete cache is being locked by downloading parties.
		 */
		TEMPORARY_FAIL,
		/** This file does not exists on the remote system. */
		FILE_NOT_FOUND
	}

	private final Code errorCode;

	/**
	 * @param string cause description
	 */
	public StagerException(String string) {
		this(Code.UNDEFINED, string);
	}

	/**
	 * @param status status code for the exception
	 * @param string cause description
	 */
	public StagerException(Code error, String string) {
		super(string);
		errorCode = error;
	}
	/**
	 * @param error exceptions error code
	 * @param string cause description
	 * @param cause wrapped throwable which caused this exception
	 */
	public StagerException(Code error, String string, Throwable cause) {
		super(string, cause);
		errorCode = error;
	}
	/**
	 * @return the statusCode
	 */
	public Code getErrorCode() {
		return errorCode;
	}

}
