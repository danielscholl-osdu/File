package org.opengroup.osdu.file.util.test;

import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;

public interface HttpClient {

	HttpResponse send(HttpRequest httpRequest);
	<T> T send(HttpRequest httpRequest, Class<T> classOfT);
}
