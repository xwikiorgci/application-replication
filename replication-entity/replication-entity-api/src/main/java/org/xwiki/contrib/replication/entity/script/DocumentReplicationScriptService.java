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
package org.xwiki.contrib.replication.entity.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.replication.ReplicationException;
import org.xwiki.contrib.replication.entity.DocumentReplicationController;
import org.xwiki.contrib.replication.entity.DocumentReplicationControllerInstance;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

/**
 * Expose some document replication related APIs.
 * 
 * @version $Id$
 */
@Component
@Named("replication.document")
@Singleton
public class DocumentReplicationScriptService implements ScriptService
{
    @Inject
    private DocumentReplicationController controller;

    /**
     * Indicate the list of registered instances this document should be replicated to.
     * 
     * @param documentReference the reference of the document about to be replicated
     * @return the registered instances on which to replicate the document
     * @throws ReplicationException when failing to get the instances
     */
    public List<DocumentReplicationControllerInstance> getDocumentInstances(DocumentReference documentReference)
        throws ReplicationException
    {
        return this.controller.getDocumentInstances(documentReference);
    }
}
