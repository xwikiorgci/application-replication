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
package org.xwiki.contrib.replication.internal.instance;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.ApplicationReadyEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.replication.ReplicationException;
import org.xwiki.contrib.replication.ReplicationInstance;
import org.xwiki.contrib.replication.ReplicationInstance.Status;
import org.xwiki.contrib.replication.ReplicationInstanceManager;
import org.xwiki.contrib.replication.ReplicationSender;
import org.xwiki.contrib.replication.event.ReplicationInstanceRegisteredEvent;
import org.xwiki.contrib.replication.event.ReplicationInstanceUnregisteredEvent;
import org.xwiki.contrib.replication.internal.message.ReplicationInstanceMessageSender;
import org.xwiki.contrib.replication.internal.message.ReplicationReceiverMessageQueue;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.XObjectEvent;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseObjectReference;
import com.xpn.xwiki.store.XWikiCacheStore;
import com.xpn.xwiki.store.XWikiStoreInterface;

/**
 * @version $Id$
 */
@Component
@Named(ReplicationInstanceListener.NAME)
@Singleton
public class ReplicationInstanceListener extends AbstractEventListener
{
    /**
     * The name of this event listener.
     */
    public static final String NAME = "ReplicationInstanceListener";

    @Inject
    private Provider<ReplicationInstanceStore> storeProvider;

    @Inject
    private Provider<ReplicationInstanceManager> instanceProvider;

    @Inject
    private Provider<ReplicationSender> senderProvider;

    @Inject
    private Provider<ReplicationInstanceMessageSender> instanceSenderProvider;

    @Inject
    private Provider<ReplicationReceiverMessageQueue> receiverProvider;

    @Inject
    private ObservationManager observation;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private RemoteObservationManagerContext remoteContext;

    @Inject
    private Logger logger;

    /**
     * Default constructor.
     */
    public ReplicationInstanceListener()
    {
        super(NAME, new ApplicationReadyEvent(),
            BaseObjectReference.anyEvents(StandardReplicationInstanceClassInitializer.CLASS_FULLNAME));
    }

    private void forceResetDocumentCache(Event event, Object source, Object data)
    {
        XWikiContext xcontext = this.xcontextProvider.get();
        if (xcontext != null) {
            XWikiStoreInterface store = xcontext.getWiki().getStore();
            if (store instanceof XWikiCacheStore) {
                ((XWikiCacheStore) store).onEvent(event, source, data);
            }
        }        
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ApplicationReadyEvent) {
            initialize();
        } else if (event instanceof XObjectEvent) {
            // Workaround https://jira.xwiki.org/browse/XWIKI-20564
            // Make sure the document in the cache is the right one in cache of remote events
            if (this.remoteContext.isRemoteState()) {
                forceResetDocumentCache(event, source, data);
            }

            ReplicationInstanceManager instancesManager = this.instanceProvider.get();

            try {
                instancesManager.reload();
            } catch (ReplicationException e) {
                this.logger.error("Failed to reload stored instances", e);
            }

            XWikiDocument document = (XWikiDocument) source;
            EntityReference objectReference = ((XObjectEvent) event).getReference();

            // Check changes made to instances
            try {
                ReplicationInstance oldInstance = handleOldInstance(document, objectReference);

                handleNewInstance(document, objectReference, oldInstance);
            } catch (ReplicationException e) {
                this.logger.error("Failed to update the instances", e);
            }
        }
    }

    private ReplicationInstance handleOldInstance(XWikiDocument document, EntityReference objectReference)
        throws ReplicationException
    {
        XWikiDocument documentOld = document.getOriginalDocument();

        ReplicationInstanceStore store = this.storeProvider.get();

        BaseObject xobjectOld = documentOld.getXObject(objectReference);

        if (xobjectOld != null) {
            Status statusOld = store.getStatus(xobjectOld);

            if (statusOld == Status.REGISTERED) {
                String uriOld = store.getURI(xobjectOld);
                ReplicationInstance instanceOld = this.instanceProvider.get().getInstanceByURI(uriOld);
                if (instanceOld == null || instanceOld.getStatus() != Status.REGISTERED) {
                    this.observation.notify(new ReplicationInstanceUnregisteredEvent(uriOld),
                        store.toReplicationInstance(xobjectOld));
                }

                return instanceOld;
            }
        }

        return null;
    }

    private void handleNewInstance(XWikiDocument document, EntityReference objectReference,
        ReplicationInstance instanceOld) throws ReplicationException
    {
        ReplicationInstanceStore store = this.storeProvider.get();

        BaseObject xobjectNew = document.getXObject(objectReference);

        if (xobjectNew != null) {
            String uriNew = store.getURI(xobjectNew);

            ReplicationInstance instanceNew = this.instanceProvider.get().getInstanceByURI(uriNew);

            // Check if the current instance has been modified
            if (instanceNew != null && instanceNew.getStatus() == null && instanceOld != null) {
                try {
                    this.instanceSenderProvider.get().updateCurrentInstance();
                } catch (Exception e) {
                    this.logger.error("Failed to send update message for current instance", e);
                }
            }

            // Check if an instance has been registered
            if (instanceNew != null && instanceNew.getStatus() == Status.REGISTERED
                && (instanceOld == null || !instanceOld.getURI().equals(uriNew))) {
                this.observation.notify(new ReplicationInstanceRegisteredEvent(uriNew), instanceNew);
            }
        }
    }

    private void initialize()
    {
        // Initialize the sender
        this.senderProvider.get();

        // Initialize the receiver
        this.receiverProvider.get();
    }
}
