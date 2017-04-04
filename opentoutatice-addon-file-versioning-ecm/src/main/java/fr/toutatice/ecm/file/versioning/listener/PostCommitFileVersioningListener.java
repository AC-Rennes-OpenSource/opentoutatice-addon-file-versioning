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
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class PostCommitFileVersioningListener implements PostCommitFilteringEventListener {

    /** File Manager. */
    private static FileManager fileManager;

    /**
     * Getter for FileManager.
     */
    public static FileManager getFileManager() {
        if (fileManager == null) {
            fileManager = (FileManager) Framework.getService(FileManager.class);
        }
        return fileManager;
    }

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

            // Versionable File
            boolean isVersionableFile = StringUtils.equals("File", srcDoc.getType()) && srcDoc.isVersionable() && !srcDoc.isImmutable();
            if (isVersionableFile) {
                // documentcreated can be sent by version creation (AbstractSession#notifyCheckedInVersion)
                // or import: we do not versioning in those cases
                if (!srcDoc.isVersion() && srcDoc.isCheckedOut()) {
                    if (getFileManager().doVersioningAfterAdd()) {
                        srcDoc.checkIn(VersioningOption.MINOR, null);
                    }
                }
            }

        }

    }

}
