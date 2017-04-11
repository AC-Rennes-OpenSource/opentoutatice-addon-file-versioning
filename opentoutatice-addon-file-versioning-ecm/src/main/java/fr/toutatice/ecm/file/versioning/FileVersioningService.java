/**
 * 
 */
package fr.toutatice.ecm.file.versioning;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelFactory;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.versioning.StandardVersioningService;


/**
 * @author david
 *
 */
public class FileVersioningService extends StandardVersioningService {

    /** Marker to create Version. */
    public static final String APPLY_OTTC_FILE_VERSIONING = "applyOttcFileVersioning";

    @Override
    public Document doPostSave(Document doc, VersioningOption option, String checkinComment, Map<String, Serializable> options) throws DocumentException {
        if (doApplyFileVersioning(doc, options)) {
            try {
                // Versioning
                incrementByOption(doc, VersioningOption.MINOR);
                return doc.checkIn(null, checkinComment);
            } finally {
                options.remove(APPLY_OTTC_FILE_VERSIONING);
            }
        } else {
            return super.doPostSave(doc, option, checkinComment, options);
        }
    }

    /**
     * A version is created for File when only its Blob has changed.
     * 
     * @param doc
     * @return true if version must be created
     * @throws DocumentException
     */
    protected boolean doApplyFileVersioning(Document doc, Map<String, Serializable> options) throws DocumentException {
        return doc.isCheckedOut() && BooleanUtils.isTrue((Boolean) options.get(APPLY_OTTC_FILE_VERSIONING));
    }

    /**
     * Gets the document model for the given core document.
     *
     * @param doc the document
     * @return the document model
     */
    protected DocumentModel readModel(Document doc) throws ClientException {
        try {
            String[] fileSchema = {"file"};
            return DocumentModelFactory.createDocumentModel(doc, fileSchema);
        } catch (DocumentException e) {
            throw new ClientException("Failed to create document model", e);
        }
    }

}
