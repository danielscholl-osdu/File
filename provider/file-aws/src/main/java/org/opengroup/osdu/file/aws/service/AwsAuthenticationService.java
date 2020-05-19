/*
 * Copyright 2020 Amazon Web Services
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

package org.opengroup.osdu.file.aws.service;

import com.amazonaws.regions.Regions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.entitlements.GroupsHelper;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.ingest.Headers;
import org.opengroup.osdu.core.common.model.file.FileRole;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.exception.OsduUnauthorizedException;
import org.opengroup.osdu.file.provider.interfaces.IAuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class AwsAuthenticationService implements IAuthenticationService {
  @Value("${aws.lambda.get-groups-function-name}")
  private String getGroupsFunctionName;

  private GroupsHelper groupsHelper;

  private final static String ACCESS_DENIED = "Access denied";
  private final static String ACCESS_DENIED_MSG = "The user is not authorized to perform this action";

  @PostConstruct
  public void createAuthenticationService(){
    groupsHelper = new GroupsHelper(Regions.US_EAST_1, getGroupsFunctionName);
  }

  @Override
  public void checkAuthentication(String authorizationToken, String dataPartitionId) {
    log.debug("Start checking authentication. Authorization: {}, partitionID: {}",
        authorizationToken, dataPartitionId);

    checkPreconditions(authorizationToken, dataPartitionId);

    Groups groups;
    try {
      groups = getGroups(authorizationToken, dataPartitionId);
    } catch (Exception e){
      throw new OsduUnauthorizedException(ACCESS_DENIED);
    }

    if (userHasFileRole(groups) == false){
      throw new OsduUnauthorizedException(ACCESS_DENIED);
    }

    log.debug("Finished checking authentication.");
  }

  private void checkPreconditions(String authorizationToken, String partitionID) {
    if (authorizationToken == null) {
      throw new OsduUnauthorizedException("Missing authorization token");
    }

    if (partitionID == null) {
      throw new OsduUnauthorizedException("Missing partitionID");
    }
  }

  private Groups getGroups(String authorizationToken, String dataPartitionId){
    Map<String, String> headers = new HashMap<>();
    headers.put(Headers.AUTHORIZATION, authorizationToken);
    // TODO: consolidate data-partition-id with partition-id (the latter provided by google)
    headers.put("data-partition-id", dataPartitionId);
    Groups groups = groupsHelper.getGroups(headers);
    return groups;
  }

  private boolean userHasFileRole(Groups groups){
    return groups.any(FileRole.ADMIN) || groups.any(FileRole.CREATOR);
  }

}
