package org.opengroup.osdu.file.provider.azure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventGridConfig {

	@Value("${azure.eventGrid.topicName.status}")
    private String statusEventGridCustomTopic;

    public String getStatusTopicName() {
        return statusEventGridCustomTopic;
    }

}
