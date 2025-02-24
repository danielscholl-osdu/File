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

package org.opengroup.osdu.file.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.partition.PartitionInfoAws;
import org.opengroup.osdu.core.aws.partition.PartitionServiceClientWithCache;
import org.opengroup.osdu.core.aws.s3.util.IS3ClientConnectionInfoHelper;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.cache.S3ConnectionInfoCache;
import org.opengroup.osdu.file.provider.aws.helper.S3ConnectionInfoHelper;

@RunWith(MockitoJUnitRunner.class)
public class S3ConnectionInfoHelperTest {

    private final String partitionID = "partitionID";
    private final String bucketParameterRelativePath = "bucketParameterRelativePath";
    private final String tenantSSMPrefix = "tenantSSMPrefix";

    @Test(expected = IllegalArgumentException.class)
    public void testGetS3ConnectionInfoForPartition_nullPartition() {

        IS3ClientConnectionInfoHelper s3ConnectionInfoHelper = mock(IS3ClientConnectionInfoHelper.class);
        S3ConnectionInfoCache s3ConnectionInfoCache = mock(S3ConnectionInfoCache.class);
        PartitionServiceClientWithCache partitionServiceClient = mock(PartitionServiceClientWithCache.class);
        DpsHeaders headers = mock(DpsHeaders.class);

        S3ConnectionInfoHelper helper = new S3ConnectionInfoHelper(s3ConnectionInfoHelper, s3ConnectionInfoCache, partitionServiceClient);

        helper.getS3ConnectionInfoForPartition(headers, bucketParameterRelativePath);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testGetS3ConnectionInfoForPartition_nullBucketPath() {

        IS3ClientConnectionInfoHelper s3ConnectionInfoHelper = mock(IS3ClientConnectionInfoHelper.class);
        S3ConnectionInfoCache s3ConnectionInfoCache = mock(S3ConnectionInfoCache.class);
        PartitionServiceClientWithCache partitionServiceClient = mock(PartitionServiceClientWithCache.class);
        DpsHeaders headers = mock(DpsHeaders.class);

        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);

        S3ConnectionInfoHelper helper = new S3ConnectionInfoHelper(s3ConnectionInfoHelper, s3ConnectionInfoCache, partitionServiceClient);

        helper.getS3ConnectionInfoForPartition(headers, null);
    }

    @Test (expected = NullPointerException.class)
    public void testGetS3ConnectionInfoForPartition_missingPartitionInfo() {

        IS3ClientConnectionInfoHelper s3ConnectionInfoHelper = mock(IS3ClientConnectionInfoHelper.class);
        S3ConnectionInfoCache s3ConnectionInfoCache = mock(S3ConnectionInfoCache.class);
        PartitionServiceClientWithCache partitionServiceClient = mock(PartitionServiceClientWithCache.class);
        DpsHeaders headers = mock(DpsHeaders.class);

        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);

        S3ConnectionInfoHelper helper = new S3ConnectionInfoHelper(s3ConnectionInfoHelper, s3ConnectionInfoCache, partitionServiceClient);

        helper.getS3ConnectionInfoForPartition(headers, bucketParameterRelativePath);
    }

    @Test
    public void testGetS3ConnectionInfoForPartition() {

        IS3ClientConnectionInfoHelper s3ConnectionInfoHelper = mock(IS3ClientConnectionInfoHelper.class);
        S3ConnectionInfoCache s3ConnectionInfoCache = mock(S3ConnectionInfoCache.class);
        PartitionServiceClientWithCache partitionServiceClient = mock(PartitionServiceClientWithCache.class);
        PartitionInfoAws partitionInfo = mock(PartitionInfoAws.class);
        DpsHeaders headers = mock(DpsHeaders.class);

        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);
        when(partitionServiceClient.getPartition(partitionID)).thenReturn(partitionInfo);
        when(partitionInfo.getTenantSSMPrefix()).thenReturn(tenantSSMPrefix);

        S3ConnectionInfoHelper helper = new S3ConnectionInfoHelper(s3ConnectionInfoHelper, s3ConnectionInfoCache, partitionServiceClient);

        helper.getS3ConnectionInfoForPartition(headers, bucketParameterRelativePath);

        verify(s3ConnectionInfoHelper, times(1)).getS3ConnectionInfoFromSSM(tenantSSMPrefix, bucketParameterRelativePath);
        verify(s3ConnectionInfoCache, times(1)).put(anyString(), any());
    }
}
