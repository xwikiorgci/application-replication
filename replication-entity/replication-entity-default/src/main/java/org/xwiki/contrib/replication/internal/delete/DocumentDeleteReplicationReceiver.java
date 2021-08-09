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
package org.xwiki.contrib.replication.internal.delete;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.replication.ReplicationException;
import org.xwiki.contrib.replication.ReplicationReceiverMessage;
import org.xwiki.contrib.replication.internal.AbstractDocumentReplicationReceiver;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @version $Id$
 */
@Component
@Singleton
@Named(DocumentDeleteReplicationMessage.TYPE)
public class DocumentDeleteReplicationReceiver extends AbstractDocumentReplicationReceiver
{
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public void receive(ReplicationReceiverMessage message) throws ReplicationException
    {
        DocumentReference documentReference = getDocumentReference(message);

        XWikiContext xcontext = this.xcontextProvider.get();

        // Load the document
        XWikiDocument document = new XWikiDocument(documentReference, documentReference.getLocale());

        try {
            xcontext.getWiki().deleteDocument(document, xcontext);
        } catch (XWikiException e) {
            throw new ReplicationException("Failed to document", e);
        }
    }
}
