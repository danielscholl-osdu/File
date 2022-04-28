/*
 * Copyright 2021 Microsoft Corporation
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
package org.opengroup.osdu.file.middleware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.file.provider.interfaces.IAuthenticationService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorizationFilterTest {
  @Mock
  IAuthorizationService authorizationService;

  @Mock
  IAuthenticationService authenticationService;

  @Mock
  AuthorizationResponse authorizationResponse;

  @Mock
  DpsHeaders headers;

  @InjectMocks
  AuthorizationFilter filter;

  @Test
  public void test_hasPermission () {
    String roles = "data.default.viewers";
    String dataPartition = "opendes";
    String authorization = "authorization";
    String user = "user";
    when(headers.getPartitionId()).thenReturn(dataPartition);
    when(headers.getAuthorization()).thenReturn(authorization);
    when(authorizationService.authorizeAny(headers, roles)).thenReturn(authorizationResponse);
    when(authorizationResponse.getUser()).thenReturn(user);

    boolean result = filter.hasPermission(roles);
    assertTrue(result);
    verify(headers, times(1)).getAuthorization();
    verify(headers, times(1)).getPartitionId();
    verify(authorizationService, times(1)).authorizeAny(headers, roles);
    verify(authorizationResponse, times(1)).getUser();
  }
}
