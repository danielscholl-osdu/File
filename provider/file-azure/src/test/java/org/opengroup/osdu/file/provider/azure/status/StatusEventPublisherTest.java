package org.opengroup.osdu.file.provider.azure.status;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.publisherFacade.MessagePublisher;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.status.StatusDetails;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.azure.config.EventGridConfig;
import org.opengroup.osdu.file.provider.azure.config.PublisherConfig;
import org.opengroup.osdu.file.provider.azure.config.ServiceBusConfig;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class StatusEventPublisherTest {

  @Mock
  private EventGridConfig eventGridConfig;

  @Mock
  private ServiceBusConfig serviceBusConfig;

  @Mock
  private PublisherConfig publisherConfig;

  @Mock
  private MessagePublisher messagePublisher;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private JaxRsDpsLog log;

  @InjectMocks
  private StatusEventPublisher statusEventPublisher;

  private Message[] messages;

  private Map<String, String> attributes;

  @BeforeEach
  public void setup() {
    Message message = new StatusDetails();
    messages = new Message[1];
    messages[0] = message;
    attributes = new HashMap<>();
    attributes.put(DpsHeaders.DATA_PARTITION_ID, "data-partition-id");
    attributes.put(DpsHeaders.CORRELATION_ID, "correlation-id");
  }

  @Test
  void testPublishWithNullData() {
    Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(null, attributes));
    then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("Nothing in message to publish");
  }

  @Test
  void testPublishWithoutDataPartitionIdInDpsHeaders() {
    Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(messages, attributes));
    then(thrown).isInstanceOf(CoreException.class)
        .hasMessageContaining("data-partition-id is required to publish status event");
  }

  @Test
  void testPublishWithoutCorrelationIdInDpsHeaders() {
    when(dpsHeaders.getPartitionId()).thenReturn("data-partition-id");
    Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(messages, attributes));
    then(thrown).isInstanceOf(CoreException.class)
        .hasMessageContaining("correlation-id is required to publish status event");
  }

  @Test
  void testPublishSuccess() {
    when(dpsHeaders.getCorrelationId()).thenReturn("correlation-id");
    when(dpsHeaders.getPartitionId()).thenReturn("data-partition-id");
    when(publisherConfig.getPubSubBatchSize()).thenReturn("1");

    statusEventPublisher.publish(messages, attributes);

    verify(messagePublisher).publishMessage(any(), any());
  }

  @Test
  void testPublishSuccessMessageArrayBiggerThanBatchSize() {
    when(dpsHeaders.getCorrelationId()).thenReturn("correlation-id");
    when(dpsHeaders.getPartitionId()).thenReturn("data-partition-id");
    when(publisherConfig.getPubSubBatchSize()).thenReturn("1");

    Message message = new StatusDetails();
    messages = new Message[2];
    messages[0] = message;
    messages[1] = message;
    statusEventPublisher.publish(messages, attributes);

    verify(messagePublisher, times(2)).publishMessage(any(), any());
  }

}
