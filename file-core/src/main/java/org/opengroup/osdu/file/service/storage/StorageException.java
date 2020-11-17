package org.opengroup.osdu.file.service.storage;

import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.DpsException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class StorageException extends DpsException {

    private static final long serialVersionUID = -3823738766134121467L;

    public StorageException(String message, HttpResponse httpResponse) {
        super(message, httpResponse);
    }
}
