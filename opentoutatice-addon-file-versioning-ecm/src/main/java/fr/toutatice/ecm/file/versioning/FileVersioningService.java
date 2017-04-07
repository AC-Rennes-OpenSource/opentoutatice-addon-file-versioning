/**
 * 
 */
package fr.toutatice.ecm.file.versioning;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelFactory;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.versioning.StandardVersioningService;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
public class FileVersioningService extends StandardVersioningService {

    /** Log. */
    private static final Log log = LogFactory.getLog(FileVersioningService.class);

    /**
     * Create.
     */
    // @Override
    // public void doPostCreate(Document doc, Map<String, Serializable> options) {
    // DocumentType type = doc.getType();
    // if (type != null && StringUtils.equals("File", type.getName())) {
    // try {
    // DocumentModel docModel = readModel(doc);
    // Blob blob = docModel.getAdapter(BlobHolder.class).getBlob();
    // if (blob != null && blob.getLength() > 0) {
    // setInitialVersion(doc);
    // Document version = doc.checkIn(null, null);
    //
    // DocumentModel versionModel = readModel(version);
    // String title = (String) docModel.getPropertyValue("dc:title");
    // versionModel.setPropertyValue("dc:title", title);
    //
    // Event evt =
    // // DublinCore ?
    // versionModel.getCoreSession().save();
    // }
    // } catch (Exception e) {
    // log.error(e);
    // }
    // } else {
    // super.doPostCreate(doc, options);
    // }
    // }

    /**
     * Update.
     */
    @Override
    protected VersioningOption validateOption(Document doc, VersioningOption option) throws DocumentException {
        DocumentType type = doc.getType();

        boolean versioned = false;
        if (type != null && StringUtils.equals("File", type.getName())) {
            try {
                DocumentModel docModel = readModel(doc);
                if (ToutaticeDocumentHelper.isInWorkspaceLike(docModel.getCoreSession(), docModel)) {
                    Property blob = docModel.getProperty("file:content");
                    if (blob.isDirty()) {
                        versioned = true;
                    }
                }
            } catch (Exception e) {
                throw new DocumentException(e);
            }
        }

        if (versioned) {
            option = VersioningOption.MINOR;
        } else {
            // Standard rule
            option = super.validateOption(doc, option);
        }

        return option;
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
