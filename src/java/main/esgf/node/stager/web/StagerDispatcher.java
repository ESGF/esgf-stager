package esgf.node.stager.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import esgf.node.stager.io.StagerCache;
import esgf.node.stager.io.StagerException;
import esgf.node.stager.utils.ExtendedProperties;

/**
 * Encapsulates the HPSS Stager that can be used as servlet or as filter.
 * 
 * @author Estanislao Gonzalez
 * 
 */
public class StagerDispatcher {

	/**
	 * This data will be added to the session.
	 * 
	 * @author Estanislao Gonzalez
	 * 
	 */
	public static class SessionData {
		private String redirectedFrom;
		private String redirectedTo;

		/**
		 * @return the redirectedFrom
		 */
		public String getRedirectedFrom() {
			return redirectedFrom;
		}

		/**
		 * @return the redirectedTo
		 */
		public String getRedirectedTo() {
			return redirectedTo;
		}
	}

	private static final Logger LOG = Logger.getLogger(StagerServlet.class);
	private static final Pattern nonInteractiveBrowsers = Pattern
			.compile("^(Wget|curl)");
	private static final Pattern interactiveBrowsers = Pattern
			.compile("^(Mozilla|Opera)");

	private transient StagerCache cache;
	private final String redirectPage;
	private final boolean dryrun;

	/**
	 * Creates a stager with the information provided in the properties
	 * 
	 * @param props
	 *            properties for setting up the stager and the underlying
	 *            objects (cache, etc)
	 * @throws StagerException
	 *             In case required configuration entries are missing
	 */
	public StagerDispatcher(ExtendedProperties props) throws StagerException {


		// try to set the cache.
		try {
			cache = new StagerCache(props);
		} catch (StagerException e) {
			throw new StagerException("Cannot instantiate the Stager.\n"
					+ e.getMessage());
		}

		redirectPage = props.getCheckedProperty("redirect");
		if (LOG.isDebugEnabled())
			LOG.debug("While wait redirecting to:" + redirectPage);

		// if set no file will be retrieved (used for testing!)
		dryrun = props.getCheckedProperty("dryrun", Boolean.FALSE);
		if (dryrun) LOG.warn("Stager started in Dry-Run Mode");

		LOG.info("Stager is up and running.");
	}

	/**
	 * Retrieves a file from HPSS into the cache and returns. If the file is
	 * already cached the call will return immediately. The call can wait until
	 * the file is in the cache (blocking in case of non-interactive client) or
	 * return immediately after scheduling its retrieval (non-blocking in case
	 * of interactive client). This differentiation is performed by analyzing
	 * the user-agent http header field.
	 * 
	 * @param hpssTarget
	 *            target file to stage
	 * @param request
	 *            the HTTP request. Used for checking User-agent and accessing
	 *            the client session.
	 * @param response
	 *            for redirecting the user in case of cache-miss and the
	 *            retrieval was triggered in non-blocking mode.
	 * @return The retrieved file or null if file is missing and retrieval
	 *         triggered in non-blocking mode.
	 * @throws IOException
	 *             in case of any IO error (cache full, etc). Normal HPSS errors
	 *             (Missing file, hpss server down, etc) are thrown as http
	 *             errors.
	 */
	File process(String hpssTarget, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		boolean blocking = !isInteractiveClient(request);

		java.io.File f = null;
		try {
			if (!dryrun)
				f = cache.retrieveFile(hpssTarget, blocking);

			HttpSession s = request.getSession(true);

			if (f == null) {
				SessionData data = (SessionData) s.getAttribute(this.getClass()
						.getName());
				if (data == null) {
					// first time here, prepare data and add to session
					data = new SessionData();
					data.redirectedFrom = request.getRequestURL().toString();
					data.redirectedTo = redirectPage;
					s.setAttribute(this.getClass().getName(), data);
				}

				if (data.redirectedTo != null) {
					response.sendRedirect(data.redirectedTo);
				}

			} else {
				// clean up session
				s.setAttribute(this.getClass().getName(), null);

				// return the local file in cache
				return f;
			}
		} catch (StagerException e) {
			response.reset();
			switch (e.getErrorCode()) {
			case PERMANENT_FAIL:
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
								.getLocalizedMessage());
				break;
			case TEMPORARY_FAIL:
				response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
						e.getMessage());
				break;
			case FILE_NOT_FOUND:
				response.sendError(HttpServletResponse.SC_NOT_FOUND, e
						.getMessage());
				break;
			default:
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
								.getMessage());
				break;
			}
		} catch (FileNotFoundException e) {
			response
					.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());

		}

		return null;
	}

	/**
	 * Retrieves a file from HPSS into the cache and returns. If the file is
	 * already cached the call will return immediately. The call can wait until
	 * the file is in the cache (blocking) or still return immediately after
	 * scheduling its retrieval.
	 * 
	 * @param hpssTarget
	 *            target file to stage
	 * @param blocking
	 *            if this call will block until the file is available
	 * @return if the file is available (always true if called in blocking mode)
	 * @throws StagerException
	 *             if retrieval from hpss fails
	 * @throws IOException
	 *             in case of any other IO error (cache full, etc)
	 */
	boolean retrieve(String hpssTarget, boolean blocking) throws StagerException,
			IOException {
		if (dryrun)
			return false;
		return cache.retrieveFile(hpssTarget, blocking) != null;
	}

	/**
	 * Tries to guess if the request was initiated from an interactive client
	 * (browser) or not (wget, curl, etc)
	 * 
	 * @param request
	 *            the HTTP request
	 * @return if this request was initiated by an interactive client
	 */
	static boolean isInteractiveClient(HttpServletRequest request) {
		String agent = request.getHeader("user-agent");
		
		//defaults to false in case nothing matches
		return interactiveBrowsers.matcher(agent).find()
				|| !nonInteractiveBrowsers.matcher(agent).find();
	}

	/**
	 * Signals that the hpss stager won't be required anymore.
	 * 
	 * @param force
	 *            force the termination
	 */
	void terminate(boolean force) {
		LOG.info("Destroying the Stager");
		cache.terminate(force);
	}
}