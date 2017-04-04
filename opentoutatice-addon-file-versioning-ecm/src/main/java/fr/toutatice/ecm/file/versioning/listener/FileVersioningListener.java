/**
 * 
 */
package fr.toutatice.ecm.file.versioning.listener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.versioning.VersioningComponent;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.file.versioning.FileVersioningService;


/**
 * @author david
 *
 */
public class FileVersioningListener implements EventListener {

    /** File versioning service. */
    private static FileVersioningService fileVersioningService;

    /**
     * Getter for FileVersioningService.
     * 
     * @throws Throwable
     */
    public static FileVersioningService getFileVersioningService() throws Throwable {
        if (fileVersioningService == null) {
            VersioningService versioning = Framework.getService(VersioningService.class);
            // FIXME: modify (or delete) proxy pattern in ottc-core:
            // here, versioning is a proxy
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(versioning);
            Method method = VersioningComponent.class.getMethod("getService", new Class<?>[0]);
            Object invoke = invocationHandler.invoke(versioning, method, new Object[0]);

            fileVersioningService = (FileVersioningService) invoke;
        }
        return fileVersioningService;
    }

    /**
     * Store dirty status of binary of a File.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        // Filter & Robustness
        if (DocumentEventTypes.BEFORE_DOC_UPDATE.equals(event.getName()) && DocumentEventContext.class.isInstance(event.getContext())) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel srcDoc = docCtx.getSourceDocument();

            // Versionable File
            boolean isVersionableFile = StringUtils.equals("File", srcDoc.getType()) && srcDoc.isVersionable();
            if (isVersionableFile) {
                // Update: binary status for versioning
                Property blobProp = srcDoc.getProperty("file:content");
                storeBlobStatus(srcDoc, blobProp.isDirty());
            }
        }
    }

    /**
     * @param srcDoc
     */
    public void storeBlobStatus(DocumentModel srcDoc, boolean status) {
        try {
            getFileVersioningService().storeBlobStatus(srcDoc.getId(), status);
        } catch (Throwable e) {
            throw new ClientException(e);
        }
    }

}
