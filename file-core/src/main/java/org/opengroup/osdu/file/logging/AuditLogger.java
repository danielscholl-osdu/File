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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;
  private final DpsHeaders headers;
  private AuditEvents events = null;

  private AuditEvents getAuditEvents() {
    if (this.events == null) {
      this.events = new AuditEvents(this.headers.getUserEmail());
    }
    return this.events;
  }

  public void readFileLocationSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadFileLocationEvent(AuditStatus.SUCCESS, resources));
  }

  public void readFileLocationFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadFileLocationEvent(AuditStatus.FAILURE, resources));
  }

  public void readFileListSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadFileListEvent(AuditStatus.SUCCESS, resources));
  }

  public void readFileListFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadFileListEvent(AuditStatus.FAILURE, resources));
  }

  public void createLocationSuccess(List<String> resources) {
    writeLog(getAuditEvents().getCreateLocationEvent(AuditStatus.SUCCESS, resources));
  }

  public void createLocationFailure(List<String> resources) {
    writeLog(getAuditEvents().getCreateLocationEvent(AuditStatus.FAILURE, resources));
  }

  private void writeLog(AuditPayload log) {
    this.logger.audit(log);
  }
}
