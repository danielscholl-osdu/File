/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.reference.factory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.file.provider.reference.config.RabbitMqConfigProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqFactory {

  private final RabbitMqConfigProperties properties;
  private Channel channel;

  @PostConstruct
  private void init() {
    ConnectionFactory factory = new ConnectionFactory();
    try {
      String uri = properties.getUri();
      log.debug(String.format("RabbitMQ Uri = %s", uri));
      factory.setUri(uri);
      factory.setAutomaticRecoveryEnabled(true);
      Connection conn = factory.newConnection();
      this.channel = conn.createChannel();
      log.debug("RabbitMQ Channel was created.");
      channel.queueDeclare(properties.getTopicName(), true, false, false, null);
      log.debug("Queue [ {} ] was declared.", properties.getTopicName());
    } catch (KeyManagementException
        | NoSuchAlgorithmException
        | URISyntaxException
        | IOException
        | TimeoutException e) {
      log.error(e.getMessage(), e);
    }
  }

  public Channel getClient() {
    return channel;
  }

  @PreDestroy
  public void destroy() throws IOException, TimeoutException {
    if (Objects.nonNull(channel) && channel.isOpen()) {
      channel.close();
    }
  }
}
