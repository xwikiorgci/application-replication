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
package org.xwiki.contrib.replication.event;

import org.xwiki.contrib.replication.ReplicationInstance;
import org.xwiki.contrib.replication.ReplicationSenderMessage;
import org.xwiki.observation.event.AbstractCancelableEvent;

/**
 * Event sent before {@link ReplicationSenderMessage} is sent to a specific instance.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the {@link ReplicationSenderMessage} about to be sent</li>
 * <li>data: the target {@link ReplicationInstance}</li>
 * </ul>
 * 
 * @version $Id$
 */
public class ReplicationSenderMessageEvent extends AbstractCancelableEvent
{
}
