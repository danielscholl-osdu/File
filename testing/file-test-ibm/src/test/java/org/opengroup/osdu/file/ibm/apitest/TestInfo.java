package org.opengroup.osdu.file.ibm.apitest;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.opengroup.osdu.file.apitest.Info;
import org.opengroup.osdu.file.ibm.util.HttpClientIBM;

public class TestInfo extends Info {

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientIBM();
  }

}
