package org.signserver.common;

/**
 * Interface used for requests to WorkerSession.process method. Should
 * be implemented by all types of signers.
 *
 * @author Philip Vendil
 * @version $Id$
 */
public interface ISignRequest {

    /**
     * Should contain a unique request id used to identify the request
     */
    int getRequestID();

    /**
     * Should contain the data that should be signed.
     */
    Object getRequestData();
}
