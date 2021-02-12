/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.logging;

import static java.lang.String.format;

import com.google.common.base.Strings;
import java.util.List;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {

  private static final String READ_FILE_LOCATION_ACTION_ID = "FL001";
  private static final String READ_FILE_LOCATION_MESSAGE = "Read file location";

  private static final String READ_FILE_LIST_ACTION_ID = "FL002";
  private static final String READ_FILE_LIST_MESSAGE = "Read file list";

  private static final String CREATE_LOCATION_ACTION_ID = "FL003";
  private static final String CREATE_LOCATION_MESSAGE = "Create location";


  private final String user;


  public AuditEvents(String user) {
    if (Strings.isNullOrEmpty(user)) {
      throw new IllegalArgumentException("User not provided for audit events.");
    }
    this.user = user;
  }

  public AuditPayload getReadFileLocationEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.READ)
        .status(status)
        .user(this.user)
        .actionId(READ_FILE_LOCATION_ACTION_ID)
        .message(getStatusMessage(status, READ_FILE_LOCATION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getReadFileListEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.READ)
        .status(status)
        .user(this.user)
        .actionId(READ_FILE_LIST_ACTION_ID)
        .message(getStatusMessage(status, READ_FILE_LIST_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getCreateLocationEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.CREATE)
        .status(status)
        .user(this.user)
        .actionId(CREATE_LOCATION_ACTION_ID)
        .message(getStatusMessage(status, CREATE_LOCATION_MESSAGE))
        .resources(resources)
        .build();
  }

  private String getStatusMessage(AuditStatus status, String message) {
    return format("%s - %s", message, status.name().toLowerCase());
  }
}
