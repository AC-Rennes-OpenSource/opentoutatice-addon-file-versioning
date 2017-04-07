/**
 * 
 */
package fr.toutatice.ecm.file.versioning.listener;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;


/**
 * @author david
 *
 */
public class PostCommitFileVersioningListener implements PostCommitFilteringEventListener {

    /**
     * Check documentCreated event and not aboutToCreate event to have dc:creator filled
     * (useful for permissions check like ToutaticeOwnerSecurityPolicy).
     */
    @Override
    public boolean acceptEvent(Event event) {
        return DocumentEventTypes.DOCUMENT_CREATED.equals(event.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        Event event = events.peek();

        // Filter & Robustness
        if (DocumentEventContext.class.isInstance(event.getContext())) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel srcDoc = docCtx.getSourceDocument();

            if (StringUtils.equals("File", srcDoc.getType()) && ToutaticeDocumentHelper.isInWorkspaceLike(docCtx.getCoreSession(), srcDoc)) {
                // Alterable document
                if (ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc)) {
                    // Versionable File
                    boolean isVersionableFile = StringUtils.equals("File", srcDoc.getType()) && srcDoc.isVersionable();
                    if (isVersionableFile && srcDoc.isCheckedOut()) {
                        // Checkin
                        srcDoc.checkIn(VersioningOption.MINOR, null);
                    }
                }
            }

        }

    }

}
