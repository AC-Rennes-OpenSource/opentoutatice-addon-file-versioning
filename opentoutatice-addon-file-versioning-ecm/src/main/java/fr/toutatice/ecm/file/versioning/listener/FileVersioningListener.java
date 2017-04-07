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
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;


/**
 * @author david
 *
 */
public class FileVersioningListener implements EventListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws ClientException {

        // Filter & Robustness
        if (DocumentEventContext.class.isInstance(event.getContext())) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel srcDoc = docCtx.getSourceDocument();

            if (StringUtils.equals("File", srcDoc.getType()) && ToutaticeDocumentHelper.isInWorkspaceLike(docCtx.getCoreSession(), srcDoc)) {
                // Alterable document
                if (ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc)) {
                    if (DocumentEventTypes.DOCUMENT_DUPLICATED.equals(event.getName())) {
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
}
