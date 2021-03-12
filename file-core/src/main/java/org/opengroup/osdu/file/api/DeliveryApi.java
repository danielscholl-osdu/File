// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.api;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.file.constant.DeliveryRole;
import org.opengroup.osdu.file.model.delivery.UrlSigningRequest;
import org.opengroup.osdu.file.model.delivery.UrlSigningResponse;
import org.opengroup.osdu.file.service.delivery.IDeliveryLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;


@RestController
@RequestScope
@Validated
@RequestMapping(value = "/v2/delivery")
public class DeliveryApi {

    @Inject
    private IDeliveryLocationService locationService;

    @Inject
    private JaxRsDpsLog logger;

    /**
     * For a provided set of one or more SRNs, determine the data file path, request signing, and return an array of
     * both the data file(s)' unsigned URI(s), as well as the signed URLs permitting short-term access
     * @param signingRequest - String arrays of the SRN(s) to get the data file URI(s) for
     * @return A web response with a String of the requested unsigned URI
     */
    @PostMapping(value = "/GetFileSignedUrl", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@authorizationFilter.hasPermission('" + DeliveryRole.VIEWER + "')")
    public ResponseEntity<UrlSigningResponse> getFileSignedURL(@RequestBody UrlSigningRequest signingRequest) {
        UrlSigningResponse urls = locationService.getSignedUrlsBySrn(signingRequest.getSrns());
        return new ResponseEntity<>(urls, HttpStatus.OK);
    }

    /**
     * Catch any requests made without the required parameter(s)
     * @param e the missing parameter exception
     * @return A web response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMyException(Exception e) {
        String responseBody = "Missing parameter: " + e.getMessage();
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
