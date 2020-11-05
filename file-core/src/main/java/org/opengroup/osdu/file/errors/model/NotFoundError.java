package org.opengroup.osdu.file.errors.model;

import lombok.AllArgsConstructor;
import org.opengroup.osdu.file.errors.ErrorDetails;

@AllArgsConstructor
public class NotFoundError extends ErrorDetails {

    public NotFoundError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("notFound");
    }
}

