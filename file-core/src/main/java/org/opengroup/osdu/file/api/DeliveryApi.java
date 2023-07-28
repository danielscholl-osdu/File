/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;


@RestController
@RequestScope
@Validated
@Hidden
@RequestMapping(value = "/v2/delivery")
@Tag(name = "delivery-api", description = "Delivery API")
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
    @Operation(summary = "${deliveryApi.getFileSignedURL.summary}", description = "${deliveryApi.getFileSignedURL.description}",
        security = {@SecurityRequirement(name = "Authorization")}, tags = { "delivery-api" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File location", content = { @Content(schema = @Schema(implementation = UrlSigningResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
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
