/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.replication.internal.enpoint.message;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.replication.ReplicationInstance;
import org.xwiki.contrib.replication.ReplicationReceiver;
import org.xwiki.contrib.replication.internal.enpoint.AbstractReplicationEndpoint;
import org.xwiki.contrib.replication.internal.enpoint.ReplicationResourceReference;
import org.xwiki.contrib.replication.internal.message.ReplicationReceiverMessageQueue;
import org.xwiki.resource.ResourceReferenceHandlerException;

/**
 * @version $Id$
 */
@Component
@Named(ReplicationMessageEndpoint.PATH)
@Singleton
public class ReplicationMessageEndpoint extends AbstractReplicationEndpoint
{
    /**
     * The path to use to access this endpoint.
     */
    public static final String PATH = "message";

    /**
     * The name of the parameter containing the identifier of the last instance which sent the message.
     */
    public static final String PARAMETER_INSTANCE = "instance";

    @Inject
    private ComponentManager componentManager;

    @Inject
    private ReplicationReceiverMessageQueue queue;

    @Inject
    private Provider<HttpServletRequestReplicationReceiverMessage> messageProvider;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, ReplicationResourceReference reference)
        throws Exception
    {
        // Make sure the sending instance is allowed to communicate with this instance
        ReplicationInstance instance = validateInstance(reference.getParameterValue(PARAMETER_INSTANCE));

        // Make sure the data type is supported
        String dataType = reference.getParameterValue(HttpServletRequestReplicationReceiverMessage.PARAMETER_TYPE);
        if (!this.componentManager.hasComponent(ReplicationReceiver.class, dataType)) {
            throw new ResourceReferenceHandlerException("Unsupported replication data type [" + dataType + "]");
        }

        // Prepare the data
        HttpServletRequestReplicationReceiverMessage message = this.messageProvider.get();
        message.initialize(instance, request);

        // Add the data to the queue
        try {
            this.queue.add(message);
        } catch (Exception e) {
            throw new ResourceReferenceHandlerException("Could not handle the replication data", e);
        }
    }
}
