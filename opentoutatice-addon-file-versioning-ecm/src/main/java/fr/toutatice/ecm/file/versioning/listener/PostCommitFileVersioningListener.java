/**
 * 
 */
package fr.toutatice.ecm.file.versioning.listener;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;


/**
 * @author david
 *
 */
public class PostCommitFileVersioningListener extends AbstractFileVersioning implements PostCommitFilteringEventListener {

    /**
     * Check documentCreated event and not aboutToCreate event to have dc:creator filled
     * (useful for permissions check like ToutaticeOwnerSecurityPolicy).
     */
    @Override
    public boolean acceptEvent(Event event) {
        return DocumentEventTypes.DOCUMENT_CREATED.equals(event.getName()) || DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName())
                || DocumentEventTypes.DOCUMENT_UPDATED.equals(event.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        for (Event event : events) {
            if (super.doVersioning(event)) {
                DocumentModel srcDoc = ((DocumentEventContext) event.getContext()).getSourceDocument();
                if (srcDoc.isCheckedOut()) {
                    // Checkin
                    srcDoc.checkIn(VersioningOption.MINOR, null);
                }

            }
        }
    }

}
