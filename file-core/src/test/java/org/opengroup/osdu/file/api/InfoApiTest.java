package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class InfoApiTest {

  @Mock
  private VersionInfoBuilder versionInfoBuilder;

  @Mock
  private VersionInfo versionInfo;

  @InjectMocks
  private InfoApi infoApi;

  @Test
  public void test_info() throws IOException {
    when(versionInfoBuilder.buildVersionInfo()).thenReturn(versionInfo);
    VersionInfo versionInfo = infoApi.info();
    assertNotNull(versionInfo);
    verify(versionInfoBuilder, times(1)).buildVersionInfo();
  }
}
