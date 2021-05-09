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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {

  @Mock
  private JaxRsDpsLog log;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  private AuditLogger sut;

  private List<String> resources;

  @Before
  public void setup() {
    when(this.headers.getUserEmail()).thenReturn("test_user@email.com");
    resources = Collections.singletonList("resources");
  }

  @Test
  public void should_writeReadFileLocationSuccessEvent() {
    this.sut.readFileLocationSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileLocationFailureEvent() {
    this.sut.readFileLocationSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileListSuccessEvent() {
    this.sut.readFileListSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadFileListFailureEvent() {
    this.sut.readFileListFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeCreateLocationSuccessEvent() {
    this.sut.createLocationSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeCreateLocationFailureEvent() {
    this.sut.createLocationFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }
}
