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

package org.opengroup.osdu.file.provider.azure.common.base;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public final class MoreObjects {
  public static <T> T firstNonNull(@NullableDecl T first, @NullableDecl T second) {
    if (first != null) {
      return first;
    } else if (second != null) {
      return second;
    } else {
      throw new NullPointerException("Both parameters are null");
    }
  }
}

