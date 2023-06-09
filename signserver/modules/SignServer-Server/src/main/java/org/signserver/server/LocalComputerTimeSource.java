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
package org.signserver.server;

import java.util.Date;
import java.util.Properties;
import org.signserver.common.RequestContext;

/**
 * Simple class implementing the ITimeSource interface taking the current time
 * from the computer clock.
 *
 * Has no defined properties.
 *
 * @author philip
 * @version $Id$
 */
public class LocalComputerTimeSource implements ITimeSource {

    /**
     * @see org.signserver.server.ITimeSource#init(java.util.Properties)
     */
    @Override
    public void init(final Properties props) {
        // No properties defined
    }

    /**
     * Method taking the local clock as time source
     * @see org.signserver.server.ITimeSource#getGenTime()
     */
    @Override
    public Date getGenTime(final RequestContext context) {
        return new Date();
    }
}
