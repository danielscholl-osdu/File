package org.opengroup.osdu.file.errors.model;

import org.opengroup.osdu.file.errors.ErrorDetails;

public class AuthorizationError extends ErrorDetails {

    public AuthorizationError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("unauthorized");
    }
}
