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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditLoggerTest {

  @Mock
  private JaxRsDpsLog log;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  private AuditLogger sut;

  private List<String> resources;

  @BeforeEach
  public void setup() {
    when(headers.getUserEmail()).thenReturn("test_user@email.com");
    resources = Collections.singletonList("resources");
  }

  @Test
  public void should_writeReadFileLocationSuccessEvent() {

    sut.readFileLocationSuccess(resources);
    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileLocationFailureEvent() {
    sut.readFileLocationSuccess(resources);

    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileListSuccessEvent() {
    sut.readFileListSuccess(resources);

    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileListFailureEvent() {
    sut.readFileListFailure(resources);

    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeCreateLocationSuccessEvent() {
    sut.createLocationSuccess(resources);

    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeCreateLocationFailureEvent() {
    sut.createLocationFailure(resources);

    verify(log, times(1)).audit(any());
  }

  @Test
  public void should_writeCreateLocationFailureEventFailure() {
    when(headers.getUserEmail()).thenReturn(null);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      sut.createLocationFailure(resources);
    });
    assertNotNull(exception);
    assertEquals("User not provided for audit events.", exception.getMessage());
  }

  @Test
  public void should_writeReadFileLocationFailure() {
    sut.readFileLocationFailure(resources);
    verify(log, times(1)).audit(any());
  }
}
