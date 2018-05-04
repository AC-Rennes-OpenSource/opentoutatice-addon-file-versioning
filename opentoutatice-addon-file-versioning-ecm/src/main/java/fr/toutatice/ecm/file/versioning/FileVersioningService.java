/**
 *
 */
package fr.toutatice.ecm.file.versioning;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.versioning.StandardVersioningService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.file.versioning.comparator.DocumentVersionCreationDateComparator;


/**
 * @author david
 *
 */
public class FileVersioningService extends StandardVersioningService {

    /** Marker to create Version. */
    public static final String APPLY_OTTC_FILE_VERSIONING = "applyOttcFileVersioning";

    /** the maximum amount of versions a document can have */
    public static final int MAX_VERSIONS = Integer.valueOf(Framework.getProperty("ottc.file.versioning.max.kept", "-1"));

    @Override
    public Document doPostSave(CoreSession session, Document doc, VersioningOption option, String checkinComment, Map<String, Serializable> options)
            throws NuxeoException {
        if (doApplyFileVersioning(doc, options)) {
            try {
                // Versioning
                incrementByOption(doc, VersioningOption.MINOR);
                Document checkedInDoc = doc.checkIn(null, checkinComment);

                // apply versions amount restriction
                if (MAX_VERSIONS >= 0) {
                    List<Document> versions = checkedInDoc.getVersions();
                    // sort chronologically
                    Collections.sort(versions, new DocumentVersionCreationDateComparator());
                    // remove older versions if more than max
                    if (versions.size() > MAX_VERSIONS) {
                        for (int i = 0; i < versions.size(); i++) {
                            if (i >= MAX_VERSIONS) {
                                Document version = versions.get(i);
                                version.remove();
                            }
                        }
                    }
                }

                return checkedInDoc;
            } finally {
                options.remove(APPLY_OTTC_FILE_VERSIONING);
            }
        } else {
            return super.doPostSave(session, doc, option, checkinComment, options);
        }
    }

    /**
     * A version is created for File when only its Blob has changed.
     *
     * @param doc
     * @return true if version must be created
     * @throws DocumentException
     */
    protected boolean doApplyFileVersioning(Document doc, Map<String, Serializable> options) throws NuxeoException {
        return doc.isCheckedOut() && BooleanUtils.isTrue((Boolean) options.get(APPLY_OTTC_FILE_VERSIONING));
    }

}
