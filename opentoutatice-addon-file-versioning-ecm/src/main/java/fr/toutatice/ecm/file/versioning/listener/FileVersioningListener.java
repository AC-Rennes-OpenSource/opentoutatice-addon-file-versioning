/**
 * 
 */
package fr.toutatice.ecm.file.versioning.listener;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.file.versioning.FileVersioningService;


/**
 * @author david
 *
 */
public class FileVersioningListener extends AbstractFileVersioning implements EventListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws NuxeoException {

        if (DocumentEventTypes.BEFORE_DOC_UPDATE.equals(event.getName()) && super.doVersioning(event)) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            // Marks document for FileVersioningService use
            docCtx.setProperty(FileVersioningService.APPLY_OTTC_FILE_VERSIONING, true);
        }
    }
}
