package org.opengroup.osdu.file.errors.model;

import lombok.AllArgsConstructor;
import org.opengroup.osdu.file.errors.ErrorDetails;

@AllArgsConstructor
public class BadRequestError extends ErrorDetails {

    public BadRequestError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("badRequest");
    }
}
