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

import org.opengroup.osdu.core.aws.partition.PartitionInfoAws;
import org.opengroup.osdu.core.aws.partition.PartitionServiceClientWithCache;
import org.opengroup.osdu.core.aws.s3.util.IS3ClientConnectionInfoHelper;
import org.opengroup.osdu.core.aws.s3.util.S3ClientConnectionInfo;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.cache.S3ConnectionInfoCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class S3ConnectionInfoHelper {

    private final IS3ClientConnectionInfoHelper iHelper;
    private final S3ConnectionInfoCache s3ConnectionInfoCache;
    private final PartitionServiceClientWithCache partitionServiceClient;

    public S3ConnectionInfoHelper(IS3ClientConnectionInfoHelper s3ConnectionInfoHelper,
                                  S3ConnectionInfoCache s3ConnectionInfoCache,
                                  @Lazy PartitionServiceClientWithCache partitionServiceClient) {
        this.iHelper = s3ConnectionInfoHelper;
        this.s3ConnectionInfoCache = s3ConnectionInfoCache;
        this.partitionServiceClient = partitionServiceClient;
    }

    public S3ClientConnectionInfo getS3ConnectionInfoForPartition(String dataPartition, String bucketParameterRelativePath) {
        if (dataPartition == null) {
            throw new IllegalArgumentException("dataPartition cannot be empty");
        }

        if (bucketParameterRelativePath == null) {
            throw new IllegalArgumentException("bucketParameterRelativePath cannot be empty");
        }

        String dataPartitionAndBucketParameterCombo = String.format("%s_%s", dataPartition, bucketParameterRelativePath);
        S3ClientConnectionInfo s3ClientConnectionInfo = s3ConnectionInfoCache.get(dataPartitionAndBucketParameterCombo);

        if (s3ClientConnectionInfo == null) {
            PartitionInfoAws partitionInfo = partitionServiceClient.getPartition(dataPartition);

            if (partitionInfo == null) {
                throw new NullPointerException("data partition info not found");
            }

            String tenantSSMPrefix = partitionInfo.getTenantSSMPrefix();
            s3ClientConnectionInfo = iHelper.getS3ConnectionInfoFromSSM(tenantSSMPrefix, bucketParameterRelativePath);
            s3ConnectionInfoCache.put(dataPartitionAndBucketParameterCombo, s3ClientConnectionInfo);
        }

        return s3ClientConnectionInfo;
    }

    public S3ClientConnectionInfo getS3ConnectionInfoForPartition(DpsHeaders headers, String bucketParameterRelativePath) {
        String dataPartition = headers.getPartitionIdWithFallbackToAccountId();

        return getS3ConnectionInfoForPartition(dataPartition, bucketParameterRelativePath);
    }
}
