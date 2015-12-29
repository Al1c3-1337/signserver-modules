/*************************************************************************
 *                                                                       *
 *  SignServer: The OpenSource Automated Signing Server                  *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.signserver.server.signers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.signserver.common.CryptoTokenOfflineException;
import org.signserver.common.IllegalRequestException;
import org.signserver.common.ProcessRequest;
import org.signserver.common.ProcessResponse;
import org.signserver.common.RequestContext;
import org.signserver.common.SignServerException;
import org.signserver.common.StaticWorkerStatus;
import org.signserver.common.WorkerStatus;
import org.signserver.server.IServices;
import org.signserver.server.ServiceExecutionFailedException;
import org.signserver.server.timedservices.ITimedService;

/**
 * Worker implementation to use as a placeholder for instance in the case a
 * worker is missing the IMPLEMENTATION_CLASS property.
 *
 * @author Markus Kilås
 * @version $Id$
 */
public class UnloadableWorker extends BaseSigner implements ITimedService {
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(UnloadableWorker.class);

    private static final String WORKER_TYPE = "Worker";

    private final String errorMessage;

    public UnloadableWorker(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * This implementation requires no certificates.
     * @return Always true.
     */
    @Override
    protected boolean isNoCertificates() {
        return true;
    }

    @Override
    public WorkerStatus getStatus(List<String> additionalFatalErrors, final IServices services) {
        WorkerStatus status = super.getStatus(additionalFatalErrors, services);
        if (status instanceof StaticWorkerStatus) {
            // Adjust worker type
            ((StaticWorkerStatus) status).getInfo().setWorkerType(WORKER_TYPE);
        }
        return status;
    }

    /**
     * Get the fatal errors for this worker.
     * @return List of errors which for this implementation always will contain
     * an error message
     */
    @Override
    public List<String> getFatalErrors() {
        LinkedList<String> errors = new LinkedList<>();
        errors.add(errorMessage);
        return errors;
    }

    /**
     * Receives requests but always throws exception as the worker is
     * misconfigured.
     * @param signRequest ignored
     * @param requestContext ignored
     * @return never
     * @throws IllegalRequestException
     * @throws CryptoTokenOfflineException
     * @throws SignServerException always as the worker is misconfigured
     */
    @Override
    public ProcessResponse processData(ProcessRequest signRequest, RequestContext requestContext) throws IllegalRequestException, CryptoTokenOfflineException, SignServerException {
        throw new SignServerException("Worker is misconfigured");
    }

    /**
     * Receives timed service requests but only logs an error.
     * @throws ServiceExecutionFailedException 
     */
    @Override
    public void work() throws ServiceExecutionFailedException {
        LOG.error("Service is misconfigured");
    }

    /** 
     * @return Always DONT_EXECUTE
     */
    @Override
    public long getNextInterval() {
        return DONT_EXECUTE;
    }

    /**
     * @return Always false
     */
    @Override
    public boolean isActive() {
        return false;
    }

    /**
     * @return Always false
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * @return No log types
     */
    @Override
    public Set<LogType> getLogTypes() {
        return Collections.emptySet();
    }
}