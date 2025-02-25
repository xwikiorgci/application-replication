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
package org.xwiki.contrib.replication.entity;

import org.xwiki.contrib.replication.ReplicationInstance;
import org.xwiki.contrib.replication.entity.internal.DocumentReplicationControllerInstanceConverter;

/**
 * Indicate how much of a document should be replication for a specific instance.
 * 
 * @version $Id$
 */
public class DocumentReplicationControllerInstance
{
    private final ReplicationInstance instance;

    private final DocumentReplicationLevel level;

    /**
     * @param instance the instance to replicate the document with
     * @param level indicate how much of the document should be replicated
     */
    public DocumentReplicationControllerInstance(ReplicationInstance instance, DocumentReplicationLevel level)
    {
        this.instance = instance;
        this.level = level;
    }

    /**
     * @return the instance to replicate the document with
     */
    public ReplicationInstance getInstance()
    {
        return this.instance;
    }

    /**
     * @return how much of the document should be replicated
     */
    public DocumentReplicationLevel getLevel()
    {
        return this.level;
    }

    @Override
    public String toString()
    {
        return DocumentReplicationControllerInstanceConverter.toString(this);
    }
}
