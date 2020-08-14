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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;

public final class BlobId implements Serializable {
  private static final long serialVersionUID = -6156002883225601925L;
  private final String container;
  private final String name;
  private final Long generation;

  private BlobId(String container, String name, Long generation) {
    this.container = container;
    this.name = name;
    this.generation = generation;
  }

  public String getContainer() {
    return this.container;
  }

  public String getName() {
    return this.name;
  }

  public Long getGeneration() {
    return this.generation;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("container", this.getContainer()).add("name", this.getName()).add("generation", this.getGeneration()).toString();
  }

  public static BlobId of(String container, String name) {
    return new BlobId((String)Preconditions.checkNotNull(container), (String)Preconditions.checkNotNull(name), (Long)null);
  }

  public static BlobId of(String container, String name, Long generation) {
    return new BlobId((String)Preconditions.checkNotNull(container), (String)Preconditions.checkNotNull(name), generation);
  }
}

