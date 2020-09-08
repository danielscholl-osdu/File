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


import org.opengroup.osdu.file.provider.azure.common.base.Preconditions;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Blob extends BlobInfo {
  private static final long serialVersionUID = -6806832496757441434L;
  private transient Storage storage;

  Blob(Storage storage, BlobInfo.BuilderImpl infoBuilder) {
    super(infoBuilder);
    this.storage = (Storage) Preconditions.checkNotNull(storage);
  }

  public Storage getStorage() {
    return this.storage;
  }

  public URL signUrl(long duration, TimeUnit unit) {
    return this.storage.signUrl(this, duration, unit);
  }

  public Blob.Builder toBuilder() {
    return new Blob.Builder(this);
  }

  public static class Builder extends BlobInfo.Builder {
    private final Storage storage;
    private final BlobInfo.BuilderImpl infoBuilder;

    Builder(Blob blob) {
      this.storage = blob.getStorage();
      this.infoBuilder = new BlobInfo.BuilderImpl(blob);
    }

    public Builder setBlobId(BlobId blobId) {
      this.infoBuilder.setBlobId(blobId);
      return this;
    }

    Builder setGeneratedId(String generatedId) {
      this.infoBuilder.setGeneratedId(generatedId);
      return this;
    }

    public Builder setContentType(String contentType) {
      this.infoBuilder.setContentType(contentType);
      return this;
    }

    public Blob build() {
      return new Blob(this.storage, this.infoBuilder);
    }
  }

}
