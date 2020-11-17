package org.opengroup.osdu.file.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class DatalakeStorageClientFactoryTest {

  @InjectMocks
  DatalakeStorageClientFactory datalakeStorageClientFactory;

  @Test
  public void test_createInstance() throws Exception {
    assertNotNull(datalakeStorageClientFactory.createInstance());
  }

  @Test
  public void test_getObjectType() throws Exception {

    assertNotNull(datalakeStorageClientFactory.getObjectType());
  }
}
