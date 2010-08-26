package esgf.node.stager.io.connectors;

import esgf.node.stager.io.StagerException;
import esgf.node.stager.utils.ExtendedProperties;

/**
 * Just creates an FTPConnector.
 *
 * @author Estanislao Gonzalez
 */
public class FTPConnectorFactory implements RemoteConnectorFactory {
    /** Save configuration for later use. */
    private ExtendedProperties config;

    /**
     * {@inheritDoc}
     */
    public RemoteConnector getInstance()
            throws StagerException {
        return new FTPConnector(config);
    }

    /**
     * {@inheritDoc}
     */
    public void setup(ExtendedProperties props) {
        config = props;
    }

}
