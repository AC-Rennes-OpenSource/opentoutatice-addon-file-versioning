/**
 * 
 */
package fr.toutatice.ecm.file.versioning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.versioning.StandardVersioningService;


/**
 * @author david
 *
 */
public class FileVersioningService extends StandardVersioningService {

    /** Storage of blob status of a File document (as dirty or not). */
    private static final Map<String, Boolean> blobStatus = new HashMap<>(1);

    /**
     * Store blob status of a File document (as dirty or not).
     * 
     * @param docId
     * @param binaryStatus
     */
    public void storeBlobStatus(String docId, boolean binaryStatus) {
        blobStatus.put(docId, Boolean.valueOf(binaryStatus));
    }

    /**
     * Return true if document is a File
     * and has its principal blob modified.
     * 
     * Note: this method is called on checkout and checkin.
     */
    @Override
    public boolean isPostSaveDoingCheckIn(Document doc, VersioningOption option, Map<String, Serializable> options) throws DocumentException {
        boolean checkin = false;

        if (doc.getType() != null && StringUtils.equals("File", doc.getType().getName())) {
            // Check blob has changed
            checkin = doc.isCheckedOut() && BooleanUtils.isTrue(blobStatus.get(doc.getUUID()));
        } else {
            // Default
            checkin = super.isPostSaveDoingCheckIn(doc, option, options);
        }

        return checkin;
    }

    /**
     * Creates a version only if document is a File
     * and has its principal binary modified.
     */
    @Override
    public Document doPostSave(Document doc, VersioningOption option, String checkinComment, Map<String, Serializable> options) throws DocumentException {
        // Minor version
        // FIXME: to parameterize
        option = VersioningOption.MINOR;
        try {
            // FIXME: parameterize checkinComment?
            return super.doPostSave(doc, option, checkinComment, options);
        } finally {
            resetBobStatus(doc);
        }
    }

    /**
     * Reset map storing binary status of a document.
     */
    // Call two time on saveDocument:
    // via isPostSaveDoingCheckIn and doPostSave
    private synchronized void resetBobStatus(Document doc) {
        blobStatus.clear();
        // Robustness
        Validate.isTrue(blobStatus.size() == 0);
    }

}
