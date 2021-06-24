package org.opengroup.osdu.file.exception;

import org.opengroup.osdu.core.common.exception.CoreException;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusPublishException extends CoreException {

    private static final long serialVersionUID = 1L;
    private final HttpStatus status;
    private final String errorMsg;

    public StatusPublishException(HttpStatus status, String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorMsg = errorMsg;
        this.status = status;
    }

    public StatusPublishException(HttpStatus status, String errorMsg) {
        this(status, errorMsg, null);
    }

    public StatusPublishException(String errorMsg, Throwable throwable) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg, throwable);
    }

    public StatusPublishException(String errorMsg) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg, null);
    }

    public StatusPublishException(Throwable throwable) {
        this(HttpStatus.INTERNAL_SERVER_ERROR.toString(), throwable);
    }

    public StatusPublishException() {
        this(HttpStatus.INTERNAL_SERVER_ERROR.toString());
    }
}