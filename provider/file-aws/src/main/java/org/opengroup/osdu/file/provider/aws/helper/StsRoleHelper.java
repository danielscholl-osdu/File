/**
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

package org.opengroup.osdu.file.provider.aws.helper;

import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.aws.partition.PartitionInfoAws;
import org.opengroup.osdu.core.aws.partition.PartitionServiceClientWithCache;
import org.opengroup.osdu.core.aws.ssm.SSMManagerUtil;
import org.opengroup.osdu.core.common.exception.NotFoundException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.cache.StsIamRoleCache;
import org.opengroup.osdu.file.provider.aws.model.StsIamInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Lazy
@Component
public class StsRoleHelper {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    private final PartitionServiceClientWithCache partitionServiceClient;
    private final StsIamRoleCache stsIamRoleCache;
    private final SSMManagerUtil ssmManagerUtil;

    public StsRoleHelper(@Lazy PartitionServiceClientWithCache partitionServiceClient,
                         @Lazy StsIamRoleCache stsIamRoleCache,
                         @Lazy SSMManagerUtil ssmManagerUtil) {
        this.partitionServiceClient = partitionServiceClient;
        this.stsIamRoleCache = stsIamRoleCache;
        this.ssmManagerUtil = ssmManagerUtil;
    }

    public String getRoleArnForPartition(String dataPartition, String iamParameterRelativePath) {
        if (dataPartition == null) {
            throw new IllegalArgumentException("dataPartition cannot be empty");
        }

        if (iamParameterRelativePath == null) {
            throw new IllegalArgumentException("iamParameterRelativePath cannot be empty");
        }

        String dataPartitionAndIamParameterCombo = String.format("%s_%s", dataPartition, iamParameterRelativePath);
        String iamArn = stsIamRoleCache.get(dataPartitionAndIamParameterCombo);

        if (iamArn == null) {
            PartitionInfoAws partitionInfo = partitionServiceClient.getPartition(dataPartition);

            if (partitionInfo == null) {
                throw new NullPointerException("data partition info not found");
            }

            String tenantSSMPrefix = partitionInfo.getTenantSSMPrefix();
            StsIamInfo stsIamInfo = getStsIamInfoFromSSM(tenantSSMPrefix, iamParameterRelativePath);
            iamArn = stsIamInfo.getArn();

            stsIamRoleCache.put(dataPartitionAndIamParameterCombo, iamArn);

        }

        return iamArn;
    }

    public String getRoleArnForPartition(DpsHeaders headers, String iamParameterRelativePath) {
        return getRoleArnForPartition(headers.getPartitionIdWithFallbackToAccountId(), iamParameterRelativePath);
    }

    private StsIamInfo getStsIamInfoFromSSM(String ssmPath) {
        List<Parameter> tableParams = ssmManagerUtil.getSsmParamsUnderPath(ssmPath);

        if (tableParams.isEmpty()) {
            throw new NotFoundException("STS IAM role info not found in SSM");
        }

        Map<String, String> tableParamsMap = tableParams
                                                 .stream()
                                                 .collect(Collectors.toMap(p -> StringUtils.substringAfterLast(p.getName(), "/"),
                                                                           Parameter::getValue));

        return convertToStsIamInfo(tableParamsMap);
    }

    public StsIamInfo getStsIamInfoFromSSM(String tenantSSMPrefix, String ssmRelativePath) {
        String ssmPath = URI.create(String.format("%s/%s/", tenantSSMPrefix, ssmRelativePath))
                            .normalize()
                            .toString();

        return getStsIamInfoFromSSM(ssmPath);
    }

    private StsIamInfo convertToStsIamInfo(final Map<String, String> tableInfoMap) {
        try {
            String propertiesStr = objectMapper.writeValueAsString(tableInfoMap);
            return objectMapper.readValue(propertiesStr, StsIamInfo.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse StsIamInfo: {}", e.getMessage());
        }

        return null;
    }
}
