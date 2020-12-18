// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.provider.aws.service;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.http.AppException;
// import org.opengroup.osdu.core.aws.multitenancy.TenantFactory;
// import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.file.provider.aws.config.AwsServiceConfig;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StorageUtilServiceImpl implements IStorageUtilService {

//   @Inject
//   TenantFactory tenantFactory;

    @Inject
    private AwsServiceConfig awsServiceConfig;

//   @Autowired
//   GoogleCloudStorageUtil googleCloudStorageUtil;


    @Override
    public String getStagingLocation(String relativePath, String partitionID) {
        if (true)
            throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");
        // TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);

        String s3Bucket = awsServiceConfig.s3DataFileStagingBucket;
        String s3DatafilePrefix = awsServiceConfig.s3DataFilePathPrefix;
        String s3Key = s3DatafilePrefix + partitionID + "/";


        return String.format("s3://%s/%s/%s", s3Bucket, s3Key, relativePath);
    }


    @Override
    public String getPersistentLocation(String relativePath, String partitionID) {
        if (true)
            throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");
        // TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);

        String s3Bucket = awsServiceConfig.s3DataFilesPersistentBucket;
        String s3DatafilePrefix = awsServiceConfig.s3DataFilePathPrefix;
        String s3Key = s3DatafilePrefix + partitionID + "/";


        return String.format("s3://%s/%s/%s", s3Bucket, s3Key, relativePath);
    }

}
