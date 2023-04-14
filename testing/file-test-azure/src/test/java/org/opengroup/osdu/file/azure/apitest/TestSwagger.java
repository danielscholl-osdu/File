package org.opengroup.osdu.file.azure.apitest;

import org.junit.jupiter.api.BeforeAll;
import org.opengroup.osdu.file.apitest.Swagger;
import org.opengroup.osdu.file.azure.HttpClientAzure;

import java.io.IOException;

public class TestSwagger extends Swagger {

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientAzure();
  }

}
