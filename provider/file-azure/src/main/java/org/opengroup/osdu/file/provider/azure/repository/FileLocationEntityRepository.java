/*
 * Copyright 2020  Microsoft Corporation
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

package org.opengroup.osdu.file.provider.azure.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.opengroup.osdu.file.provider.azure.model.entity.FileLocationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class FileLocationEntityRepository {

  @Autowired
  private CosmosStore cosmosStore;

  @Autowired
  private String fileLocationContainer;

  @Autowired
  private String cosmosDBName;

  @Autowired
  private DpsHeaders headers;

  @Nullable
  FileLocationEntity findByFileID(String fileID) {
    if (fileID == null) {
      return null;
    }
    Optional<FileLocationEntity> fileLocationEntity = cosmosStore.findItem(headers.getPartitionId(),cosmosDBName,fileLocationContainer,fileID,fileID,FileLocationEntity.class);
    if (!fileLocationEntity.isPresent())
      return null;
    return fileLocationEntity.get();
  }

  public FileLocationEntity save(FileLocationEntity entity) {
      if (entity == null) {
        throw new IllegalArgumentException("The given file location entity is null");
      }
      // Internal set of id
      entity.setId(entity.getFileID());
      cosmosStore.upsertItem(headers.getPartitionId(), cosmosDBName, fileLocationContainer, entity.getId(), entity);
      return entity;
  }

  Page<FileLocationEntity> findFileList(@Param("time_from") Date from, @Param("time_to") Date to,
                                        @Param("user_id") String userID, Pageable pageable) {
    Timestamp fromTimestamp = new Timestamp(from.getTime());
    Timestamp toTimestamp = new Timestamp(to.getTime());

    int pageSize = pageable.getPageSize();
    int pageNum = pageable.getPageNumber();
    int offset = pageNum * pageSize;

    SqlQuerySpec query = new SqlQuerySpec("SELECT * FROM FileLocationEntity f WHERE f.createdAt >= @time_from AND f.createdAt <= @time_to AND f.createdBy = @user_id ORDER BY f.createdAt OFFSET @offset LIMIT @limit");
    List<SqlParameter> pars = query.getParameters();
    pars.add(new SqlParameter("@time_from", fromTimestamp));
    pars.add(new SqlParameter("@time_to", toTimestamp));
    pars.add(new SqlParameter("@user_id", userID));
    pars.add(new SqlParameter("@offset", offset));
    pars.add(new SqlParameter("@limit", pageSize));
    CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

    List<FileLocationEntity> fileLocationEntityList = cosmosStore.queryItems(headers.getPartitionId(), cosmosDBName, fileLocationContainer, query,
        cosmosQueryRequestOptions, FileLocationEntity.class);

    return new PageImpl(fileLocationEntityList,  pageable, fileLocationEntityList.size());
  }

}
