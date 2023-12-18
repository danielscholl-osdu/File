/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.provider.repository;

import static java.lang.String.format;

import static org.opengroup.osdu.core.osm.core.model.where.predicate.Eq.eq;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.core.osm.core.model.order.OrderBy;
import org.opengroup.osdu.core.osm.core.model.query.GetQuery;
import org.opengroup.osdu.core.osm.core.model.where.condition.And;
import org.opengroup.osdu.core.osm.core.model.where.predicate.Eq;
import org.opengroup.osdu.core.osm.core.model.where.predicate.Ge;
import org.opengroup.osdu.core.osm.core.model.where.predicate.Le;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.provider.gcp.config.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.gcp.model.FileLocationOsm;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OsmFileLocationRepository implements IFileLocationRepository {

  private static final String CREATED_AT = "createdAt";
  private static final String CREATED_BY = "createdBy";
  private static final String FILE_ID = "fileID";

  private final Context osmDatabaseContext;
  private final GcpConfigurationProperties configurationProperties;
  private final TenantInfo tenantInfo;
  private final Random random = new Random();

  @Override
  public FileLocation findByFileID(String fileID) {
    if (Objects.isNull(fileID)) {
      return null;
    }
    GetQuery<FileLocationOsm> fileLocationGetQuery =
        new GetQuery<>(FileLocationOsm.class, getDestination(),
            eq(FILE_ID, fileID));
    List<FileLocationOsm> resultsAsList = osmDatabaseContext.getResultsAsList(fileLocationGetQuery);
    Optional<FileLocationOsm> locationOsm = resultsAsList.stream().findFirst();
    return locationOsm.map(FileLocationOsm::toFileLocation).orElse(null);
  }

  @Override
  public FileLocation save(FileLocation fileLocation) {
    this.random.setSeed(fileLocation.hashCode());
    long aLong = random.nextLong();
    FileLocationOsm fileLocationOsm = new FileLocationOsm(fileLocation, aLong);
    return osmDatabaseContext.createAndGet(fileLocationOsm, getDestination()).toFileLocation();
  }

  //  TODO refactor after pagination implemented in osm
  @Override
  public FileListResponse findAll(FileListRequest request) {
    int pageSize = request.getItems();
    int pageNum = request.getPageNum();

    GetQuery<FileLocationOsm>.GetQueryBuilder<FileLocationOsm> queryBuilder =
        new GetQuery<>(FileLocationOsm.class, getDestination()).toBuilder();

    And and =
        And.and(
            Ge.ge(CREATED_AT,
                Date.from(request.getTimeFrom().toInstant(ZoneOffset.UTC)).getTime()),
            Le.le(CREATED_AT,
                Date.from(request.getTimeTo().toInstant(ZoneOffset.UTC)).getTime()),
            Eq.eq(CREATED_BY, request.getUserID())
        );
    OrderBy orderBy = OrderBy.builder().addAsc(CREATED_AT).build();
    GetQuery<FileLocationOsm> build = queryBuilder.where(and).orderBy(orderBy).build();
    List<FileLocationOsm> resultsAsList = osmDatabaseContext.getResultsAsList(build);
    if (resultsAsList.isEmpty()) {
      throw new FileLocationNotFoundException(
          format("Nothing found for such filter and page(num: %s, size: %s).", pageNum, pageSize));
    }
    PagedListHolder<FileLocation> pagedListHolder =
        new PagedListHolder<>(
            resultsAsList.stream()
                .map(FileLocationOsm::toFileLocation)
                .collect(Collectors.toList())
        );

    pagedListHolder.setPageSize(pageSize);
    pagedListHolder.setPage(pageNum);

    return FileListResponse.builder()
        .content(pagedListHolder.getPageList())
        .size(pagedListHolder.getPageSize())
        .number(pagedListHolder.getPage())
        .numberOfElements(pagedListHolder.getNrOfElements())
        .build();
  }

  private Destination getDestination() {
    return Destination.builder()
        .partitionId(tenantInfo.getDataPartitionId())
        .namespace(new Namespace(tenantInfo.getName()))
        .kind(new Kind(configurationProperties.getFileLocationKind()))
        .build();
  }
}
