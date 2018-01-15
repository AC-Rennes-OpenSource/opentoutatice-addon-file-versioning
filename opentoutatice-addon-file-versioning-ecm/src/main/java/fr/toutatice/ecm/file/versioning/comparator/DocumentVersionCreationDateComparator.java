package fr.toutatice.ecm.file.versioning.comparator;

import java.util.Comparator;

import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.model.Document;

/**
 * Simple comparator de documents, n'accepte que des versions
 * ordre chronologique inversé basé sur la date de création de version
 *
 * @author Dorian Licois
 */
public class DocumentVersionCreationDateComparator implements Comparator<Document> {

    @Override
    public int compare(Document o1, Document o2) {
        try {
            return o2.getVersionCreationDate().compareTo(o1.getVersionCreationDate());
        } catch (DocumentException e) {
            return 0;
        }
    }

}
