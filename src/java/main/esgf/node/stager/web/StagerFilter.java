package esgf.node.stager.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esgf.node.stager.io.StagerException;
import esgf.node.stager.utils.ExtendedProperties;

/**
 * Stage files from the remote system while being accessed.
 * 
 * @author Estanislao Gonzalez
 */
public class StagerFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(StagerFilter.class);
	private static final boolean DEBUG = LOG.isDebugEnabled();
	/**
	 * Filter init parameter pointing to the relative path (from this
	 * application) of the configuration file.
	 */
	public static final String PARAM_CONFIG_FILE = "configurationFile";
	
	public static final String PROP_PRE = "filter.";
	public static final String PROP_SERV = "service";
	public static final String PROP_SERV_PATTERN = ".pattern";

	private final Map<String, Pattern> services = new HashMap<String, Pattern>();
	private StagerDispatcher stager;

	public void destroy() {
		stager.terminate(false);
		LOG.info("Filter destroyed");
	}

	/**
	 * {@inheritDoc}
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		if (req instanceof HttpServletRequest
				&& res instanceof HttpServletResponse) {

			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;

			if (DEBUG)
				LOG.debug("Filtering: " + request.getServletPath().substring(1)
						+ ":" + request.getPathInfo());

			// we have a servlet
			String servlet = request.getServletPath().substring(1);
			String path = request.getPathInfo();

			Pattern p = services.get(servlet);
			if (p != null) {
				// proceed to extract the filename
				Matcher m = p.matcher(path);
				if (m.find()) {
					String fileName = m.group(1);

					if (isStaged(fileName)) {
						if (LOG.isDebugEnabled())
							LOG.debug("Filter match: " + fileName);

						// stage file
						stager.process(fileName, request, response);
					} else {
						if (DEBUG)
							LOG.debug("File not in Staged system: " + fileName);
					}

				} else {
					if (DEBUG)
						LOG.debug("No filter match(" + p.pattern()
								+ ") for path: " + path);

				}
			} else {
				if (DEBUG)
					LOG.debug("No patern for servlet: " + servlet);

			}

		}

		chain.doFilter(req, res);

		// the resulting page is in res
		// we might inject code...
	}

	/**
	 * Checks if the current file is being held at the remote system.
	 * 
	 * @param fileName the filename to check (DRS structure in case of CMIP5,
	 *            i.e. path and filename)
	 * @return if this file is being staged
	 */
	private boolean isStaged(String fileName) {
		// TODO this will probably connect to a DB to check if this is the case
		// we could implement a regexp if we get a proper algorithm to
		// differentiate the files. For the time being every hit to this filter
		// implies the file is staged.
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(FilterConfig config) throws ServletException {
		String propFilePath = config.getInitParameter(PARAM_CONFIG_FILE);
		if (propFilePath == null) 
			throw new ServletException(
					"Missing configuration file parameter (set in '"
							+ PARAM_CONFIG_FILE + "' init param)");
		// load properties
		String propPath = config.getServletContext().getRealPath(
				propFilePath);

		
		ExtendedProperties props;
		try {
			try {
				props = new ExtendedProperties(propPath);
			} catch (FileNotFoundException e1) {
				throw new ServletException("Missing configuration file: "
						+ propPath);
			}
			
			String[] services = ((String) props.getCheckedProperty(PROP_PRE
					+ PROP_SERV)).split(",");
			for (int i = 0; i < services.length; i++) {
				String pat = ((String) props.getCheckedProperty(PROP_PRE
						+ services[i] + PROP_SERV_PATTERN));

				try {
					this.services.put(services[i], Pattern.compile(pat));
				} catch (PatternSyntaxException e) {
					throw new ServletException("Syntax error at Pattern '"
							+ pat + "' :" + e.getMessage());
				}
			}
			LOG.info("Filter initiated with properties from: " + propPath);

			stager = new StagerDispatcher(props);

		} catch (FileNotFoundException e) {
			LOG.error("Filter properties not found (" + propPath + ")");
			throw new ServletException("Filter properties not found ("
					+ propPath + ")", e);
		} catch (StagerException e) {
			LOG.error("Error while creating the stager", e);
			throw new ServletException("Error while creating the stager"
					, e);
		} catch (IOException e) {
			LOG.error("Error accesing Filter properties (" + propPath + ")");
			throw new ServletException("Error accesing Filter properties ("
					+ propPath + ")", e);
		}

	}

}
