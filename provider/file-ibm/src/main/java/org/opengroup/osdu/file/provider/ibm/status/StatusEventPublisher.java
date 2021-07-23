package org.opengroup.osdu.file.provider.ibm.status;

import java.util.Map;

import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusEventPublisher implements IEventPublisher {

	private final JaxRsDpsLog log;

	@Override
	public void publish(Message[] messages, Map<String, String> attributes) throws CoreException {
		// TODO This method is not implemented yet so instead of publishing events it only does logging.
		String correlationId = attributes.get(DpsHeaders.CORRELATION_ID);
		String dataPartitionId = attributes.get(DpsHeaders.DATA_PARTITION_ID);
		log.info(DpsHeaders.CORRELATION_ID + " " + correlationId + DpsHeaders.DATA_PARTITION_ID + " " + dataPartitionId
				+ " status msgs: " + messages);
	}

}
