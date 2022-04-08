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

package org.opengroup.osdu.file.provider.gcp.config.obm;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.partition.IPartitionProvider;
import org.opengroup.osdu.core.common.partition.PartitionException;
import org.opengroup.osdu.core.common.partition.PartitionInfo;
import org.opengroup.osdu.core.common.partition.Property;
import org.opengroup.osdu.core.destination.obm.MinioObmConfigurationProperties;
import org.opengroup.osdu.core.gcp.osm.translate.TranslatorRuntimeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "obmDriver", havingValue = "minio")
public class MinioEnvironmentResolverImpl implements EnvironmentResolver {

  private static final String ENDPOINT = ".endpoint";
  private final IPartitionProvider partitionProvider;
  private final MinioObmConfigurationProperties properties;
  private final HashMap<String, PartitionInfo> partitionInfoHashMap = new HashMap<>();

  @Override
  public String getProviderKey() {
    return "ANTHOS";
  }

  @Override
  public String getTransferProtocol(String partitionId) {
    PartitionInfo partitionInfo =
        partitionInfoHashMap.computeIfAbsent(partitionId, partition -> {
          try {
            return partitionProvider.get(partitionId);
          } catch (PartitionException e) {
            throw new TranslatorRuntimeException(e, "Partition '%s' destination resolution issue",
                partitionId);
          }
        });
    Property minioUrl = partitionInfo.getProperties().get(
        properties.getPartitionPropertiesPrefix() + ENDPOINT);
    return minioUrl.getValue().toString();
  }
}
