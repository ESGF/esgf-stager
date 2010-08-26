package esgf.node.stager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import esgf.node.stager.utils.ExtendedProperties;

/**
 * Servlet implementation for the stager. This implementation was abandoned and
 * might not be working properly anymore. It is still conserved as it might be
 * required in the future.
 */
public class StagerServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(StagerServlet.class);

    private static final long serialVersionUID = 1L;

    private StagerDispatcher stager;
    private Pattern mappingThreddsHpss = null;
    private String mappingThreddsHpssReplace = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public StagerServlet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        //load properties
        String propPath= config.getServletContext().getRealPath(
                config.getInitParameter("HPSSConfigLocation"));

        ExtendedProperties props;
        if (propPath == null)
            throw new ServletException("Missing configuration file (set in HPSSConfigLocation init param)");

        try {
            props = new ExtendedProperties(propPath);
            LOG.info("Properties loaded from: " + propPath);
            stager = new StagerDispatcher(props);
            LOG.info("stager setup");

        } catch (FileNotFoundException e1) {
            LOG.error("Filter properties not found (" + propPath + ")");
            throw new ServletException("Filter properties not found (" + propPath + ")");
        } catch (IOException e1) {
            LOG.error("Error accesing Filter properties (" + propPath + ")");
            throw new ServletException("Error accesing Filter properties (" + propPath + ")");
        }

        //now parse the properties specially required for this servlet
        String regex = config.getInitParameter("mapping_from_thredds");
        if (regex != null) {
            //we have some mapping defined. Check it is valid.
            mappingThreddsHpss = Pattern.compile(regex);
            mappingThreddsHpssReplace = config.getInitParameter("mapping_to_hpss");

            if (mappingThreddsHpssReplace == null)
                throw new ServletException("Mapping incomplete. mapping_to_hpss is not set.");

            //make a test to be sure it is well formed.
            mappingThreddsHpss.matcher(".").replaceFirst(mappingThreddsHpssReplace);
        }

        LOG.info("HPSS Loader is up and running.");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();

        stager.terminate(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String hpssTarget;
        if (mappingThreddsHpss != null) {
            //remap if required (not being used anymore...)
            hpssTarget = mappingThreddsHpss.matcher(request.getPathInfo())
                            .replaceFirst(mappingThreddsHpssReplace);
        } else {
            hpssTarget = request.getPathInfo();
        }

        File f = stager.process(hpssTarget, request, response);
        if (f != null) {
            // we have the file!
            response.reset();
            response.setContentType("application/x-netcdf");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + f.getName());

            // send data
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(f);
            byte[] buff = new byte[4096];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
