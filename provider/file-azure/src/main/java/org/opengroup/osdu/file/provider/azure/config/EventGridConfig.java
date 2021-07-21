package org.opengroup.osdu.file.provider.azure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventGridConfig {

	@Value("${azure.eventGrid.enabled.status}")
    private boolean statusEventGridEnabled;

	@Value("${azure.eventGrid.topicName.status}")
    private String statusEventGridCustomTopic;

    public boolean isStatusEventGridEnabled() {
        return statusEventGridEnabled;
    }

    public String getStatusTopicName() {
        return statusEventGridCustomTopic;
    }

}
