package org.opengroup.osdu.file.errors.model;

import lombok.AllArgsConstructor;
import org.opengroup.osdu.file.errors.ErrorDetails;

@AllArgsConstructor
public class InternalServerError extends ErrorDetails {

    public InternalServerError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("internalError");
    }
}

