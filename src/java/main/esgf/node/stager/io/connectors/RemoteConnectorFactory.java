package esgf.node.stager.io.connectors;

import esgf.node.stager.io.StagerException;
import esgf.node.stager.utils.ExtendedProperties;

/**
 * Interface for factories capable of creating RemoteConnector objects from
 * given config properties. This implementation also simplifies the creation of
 * object pools and object reusal.
 *
 * @author Estanislao Gonzalez
 */
public interface RemoteConnectorFactory {

    /**
     * Retrieves a RemoteConnector instance.
     *
     * @return a properly initialized remote connector
     * @throws StagerException if the initialization fails
     */
    RemoteConnector getInstance() throws StagerException;

    /**
     * Allows to setup the factory by passing the read properties.
     *
     * @param props configuration properties
     */
    void setup(ExtendedProperties props);
}
