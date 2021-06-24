package org.opengroup.osdu.file.provider.interfaces;

import java.util.Map;

import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.exception.StatusPublishException;

public interface IStatusEventPublisher extends IEventPublisher {
    void publish(String message, Map<String, String> attributesMap) throws StatusPublishException;
}
