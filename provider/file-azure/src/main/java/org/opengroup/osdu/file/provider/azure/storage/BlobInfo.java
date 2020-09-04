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

package org.opengroup.osdu.file.provider.azure.storage;


import com.google.api.client.util.Data;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;


public class BlobInfo implements Serializable {
  private static final long serialVersionUID = -5625857076205028976L;
  private final BlobId blobId;
  private final String generatedId;
  private final String contentType;

  public BlobInfo(BuilderImpl builder) {
    this.blobId = builder.blobId;
    this.generatedId = builder.generatedId;
    this.contentType = builder.contentType;
  }

  public BlobId getBlobId() {
    return this.blobId;
  }

  public String getContainer() {
    return this.getBlobId().getContainer();
  }

  public String getGeneratedId() {
    return this.generatedId;
  }

  public String getName() {
    return this.getBlobId().getName();
  }

  public static Builder newBuilder(BlobId blobId) {
    return new BuilderImpl(blobId);
  }

  static final class BuilderImpl extends Builder {
    private BlobId blobId;
    private String generatedId;
    private String contentType;

    BuilderImpl(BlobId blobId) {
      this.blobId = blobId;
    }

    BuilderImpl(BlobInfo blobInfo) {
      this.blobId = blobInfo.blobId;
      this.generatedId = blobInfo.generatedId;
      this.contentType = blobInfo.contentType;
    }

    public Builder setBlobId(BlobId blobId) {
      this.blobId = (BlobId)Preconditions.checkNotNull(blobId);
      return this;
    }

    public Builder setGeneratedId(String generatedId) {
      this.generatedId = generatedId;
      return this;
    }


    public Builder setContentType(String contentType) {
      this.contentType = (String)MoreObjects.firstNonNull(contentType, Data.nullOf(String.class));
      return this;
    }

    public BlobInfo build() {
      Preconditions.checkNotNull(this.blobId);
      return new BlobInfo(this);
    }
  }

  public abstract static class Builder {
    public Builder() {
    }

    public abstract Builder setContentType(String var1);

    public abstract BlobInfo build();
  }


}

