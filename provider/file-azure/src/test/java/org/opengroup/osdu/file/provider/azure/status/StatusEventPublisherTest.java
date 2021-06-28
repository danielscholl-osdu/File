package org.opengroup.osdu.file.provider.azure.status;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
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
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.azure.config.EventGridConfig;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class StatusEventPublisherTest {

	@Mock
	private EventGridTopicStore eventGridTopicStore;
	
	@Mock
	private EventGridConfig eventGridConfig;
	
	@Mock
	private JaxRsDpsLog log;
	
	@InjectMocks
	private StatusEventPublisher statusEventPublisher;
	
	private String data;
	
	private Map<String, String> attributes;
	
	@BeforeEach
	public void setup() {
		data = "{ \"data\": { \"field\": \"value\" } }";
		attributes = new HashMap<String, String>();
		attributes.put(DpsHeaders.DATA_PARTITION_ID, "data-partition-id");
		attributes.put(DpsHeaders.CORRELATION_ID, "correlation-id");
		when(eventGridConfig.isEventGridEnabled()).thenReturn(true);
		
	}

	@Test
	public void testPublishWhenEventGridIsNotEnabled() {
		when(eventGridConfig.isEventGridEnabled()).thenReturn(false);
		Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(data, attributes));
		then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("Event grid is not enabled");
	}
	
	@Test
	public void testPublishWithNullData() {
		Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(null, attributes));
		then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("Nothing in message to publish");
	}
	
	@Test
	public void testPublishWithNullAttributes() {
		Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(data, null));
		then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("data-partition-id and correlation-id are required to publish status event");
	}
	
	@Test
	public void testPublishWithoutDataPartitionIdInAttributes() {
		attributes.remove(DpsHeaders.DATA_PARTITION_ID);
		Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(data, attributes));
		then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("data-partition-id is required to publish status event");
	}
	
	@Test
	public void testPublishWithoutCorrelationIdInAttributes() {
		attributes.remove(DpsHeaders.CORRELATION_ID);
		Throwable thrown = catchThrowable(() -> statusEventPublisher.publish(data, attributes));
		then(thrown).isInstanceOf(CoreException.class).hasMessageContaining("correlation-id is required to publish status event");
	}
	
	@Test
	public void testPublishSuccess() {
		statusEventPublisher.publish(data, attributes);
		
		verify(eventGridTopicStore).publishToEventGridTopic(any(), any(), any());
	}

}
