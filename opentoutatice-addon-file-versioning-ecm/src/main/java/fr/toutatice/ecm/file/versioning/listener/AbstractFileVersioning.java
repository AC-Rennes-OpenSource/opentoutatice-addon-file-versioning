/**
 * 
 */
package fr.toutatice.ecm.file.versioning.listener;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;


/**
 * @author david
 *
 */
public abstract class AbstractFileVersioning {

    /**
     * Check if version must be create on event.
     * 
     * @param event
     * @return true if version must be create on event
     */
    protected boolean doVersioning(Event event) {
        boolean doVersioning = false;

        // Filter & Robustness
        if (DocumentEventContext.class.isInstance(event.getContext())) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel srcDoc = docCtx.getSourceDocument();

            // File in Worksapce like
            if (StringUtils.equals("File", srcDoc.getType()) && ToutaticeDocumentHelper.isInWorkspaceLike(docCtx.getCoreSession(), srcDoc)) {
                // Alterable document
                if (ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc) && srcDoc.isVersionable()) {
                    if (!DocumentEventTypes.BEFORE_DOC_UPDATE.equals(event.getName())) {
                        doVersioning = true;
                    } else {
                        BlobProperty blobP = (BlobProperty) srcDoc.getProperty("file:content");
                        doVersioning = blobP.isDirty();
                    }
                }
            }
        }

        return doVersioning;
    }

}
