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
package org.opengroup.osdu.file.service.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class DataLakeStorageFactoryTest {

  @Mock
  private StorageAPIConfig config;

  @Mock
  private HttpResponseBodyMapper bodyMapper;

  @Mock
  private DpsHeaders dpsHeaders;

  @BeforeEach
  public void init() {

  }

  @Test
  public void createReturnsDataLakeStorageServiceObject() {
    DataLakeStorageFactory factory = new DataLakeStorageFactory(config, bodyMapper);
    DataLakeStorageService service = factory.create(dpsHeaders);

    Assertions.assertNotNull(service);

  }
  @Test
  public void createThrowsNPE() {
    DataLakeStorageFactory factory = new DataLakeStorageFactory(config, bodyMapper);

    NullPointerException exception = assertThrows(NullPointerException.class, () -> {
      factory.create(null);
    });
    Assertions.assertEquals("headers cannot be null", exception.getMessage());
  }

  @Test
  public void IllegalArgumentExceptionInDataLakeStorageFactoryConstructor() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      DataLakeStorageFactory factory = new DataLakeStorageFactory(null, bodyMapper);
    });
    Assertions.assertEquals("StorageAPIConfig cannot be empty", exception.getMessage());
  }

}
