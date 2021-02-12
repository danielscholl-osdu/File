/*
 * Copyright 2020 Google LLC
 * Copyright © 2021 Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.di;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.file.model.property.EntitlementProperties;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EntitlementsClientFactory extends AbstractFactoryBean<IEntitlementsFactory> {

  final EntitlementProperties entitlementProperties;

  private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpResponseBodyMapper bodyMapper = new HttpResponseBodyMapper(objectMapper);

  @Override
  protected IEntitlementsFactory createInstance() {
    return new EntitlementsFactory(EntitlementsAPIConfig.builder()
        .rootUrl(entitlementProperties.getUrl())
        .apiKey(entitlementProperties.getAppKey())
        .build(),
        bodyMapper);
  }

  @Override
  public Class<?> getObjectType() {
    return IEntitlementsFactory.class;
  }
}
