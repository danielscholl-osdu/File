/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.gcp.config;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Collections;
import org.opengroup.osdu.core.osm.core.persistence.IdentityTranslator;
import org.opengroup.osdu.core.osm.core.translate.Instrumentation;
import org.opengroup.osdu.core.osm.core.translate.TypeMapper;
import org.opengroup.osdu.file.provider.gcp.model.FileLocationOsm;
import org.springframework.stereotype.Component;

@Component

public class OsmTypeMapper extends TypeMapper {

  public OsmTypeMapper() {
    super(ImmutableList.of(
        new Instrumentation<>(
            FileLocationOsm.class,
            Collections.emptyMap(),
            ImmutableMap.of(
                "createdAt", Timestamp.class
            ),
            new IdentityTranslator<>(
                FileLocationOsm::getId,
                (f, o) -> f.setId(((Key) o).getId())
            ), Collections.singletonList("id")
        )));
  }
}
