// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.file.provider.aws.config;

import org.opengroup.osdu.core.aws.ssm.ParameterStorePropertySource;
import org.opengroup.osdu.core.aws.ssm.SSMConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AwsServiceConfig {

  @Value("${aws.s3.signed-url.expiration-days}")
  public int s3SignedUrlExpirationTimeInDays;

  @Value("${aws.region}")
  public String amazonRegion;

  @Value("${aws.resource.prefix}")
  public String environment;

  @Value("${aws.s3.endpoint}")
  public String s3Endpoint;
  
  @Value("${aws.s3.datafiles.path-prefix}")
  public String s3DataFilePathPrefix;

  @Value("${aws.s3.datafiles.staging-bucket}")
  public String s3DataFileStagingBucket;

  @Value("${aws.s3.datafiles.persistent-bucket}")
  public String s3DataFilesPersistentBucket;

  // @Value("${aws.sts-role-arn}")
  // public String stsRoleArn;

  @Value("${aws.ssm}")
  public Boolean ssmEnabled;

  @PostConstruct
  public void init() {
    if (ssmEnabled) {
      SSMConfig ssmConfig = new SSMConfig();
      ParameterStorePropertySource ssm = ssmConfig.amazonSSM();
      // String parameter = "/osdu/" + environment + "/file/iam/arn";
      try {
        // stsRoleArn = ssm.getProperty(parameter).toString();
      } catch (Exception e) {
        Exception r = e;
      }
    }

    if (s3SignedUrlExpirationTimeInDays == 0) {
      s3SignedUrlExpirationTimeInDays = 1; //default to 1 if unset
    }
  }

}
