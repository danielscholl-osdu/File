package org.opengroup.osdu.file.stepdefs.model;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpResponse {
    @Builder.Default
    Map<String, List<String>> responseHeaders = new HashMap<>();
    private int code;
    private Exception exception;
    private String body;
}
