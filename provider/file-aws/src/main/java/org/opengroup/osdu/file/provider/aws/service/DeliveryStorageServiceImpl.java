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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.provider.aws.config.AwsServiceConfig;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.model.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.util.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.util.InstantHelper;
import org.opengroup.osdu.file.provider.aws.util.S3Helper;
import org.opengroup.osdu.file.provider.aws.util.STSHelper;
import org.opengroup.osdu.file.provider.interfaces.delivery.IDeliveryStorageService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryStorageServiceImpl implements IDeliveryStorageService {  


  @Override
  public SignedUrl createSignedUrl(String srn, String unsignedUrl, String authorizationToken) {
    throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unsupported Operation Exception", "Unsupported Operation Exception");
  }

  @Override
  public SignedUrl createSignedUrl(String unsignedUrl, String authorizationToken) {
    throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unsupported Operation Exception", "Unsupported Operation Exception");
  }

}
