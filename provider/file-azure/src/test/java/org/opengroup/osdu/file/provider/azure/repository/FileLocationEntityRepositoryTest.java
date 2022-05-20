package org.opengroup.osdu.file.provider.azure.repository;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.model.entity.FileLocationEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class FileLocationEntityRepositoryTest {

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  FileLocationEntityRepository fileLocationEntityRepository;


  @Test
  public void testFindByFileIDWithNull() {

    FileLocationEntity fileLocationEntity = fileLocationEntityRepository.findByFileID(null);
    assertEquals(fileLocationEntity,null);
  }

  @Test
  public void testFindByFileIDNotNull() {
    FileLocationEntity fileLocationEntityExpected = new FileLocationEntity();
    fileLocationEntityExpected.setFileID(TestUtils.FILE_ID);
    when(cosmosStore.findItem(any(),any(),any(),any(),any(),
        any())).thenReturn(Optional.ofNullable(fileLocationEntityExpected));
    FileLocationEntity fileLocationEntityActual = fileLocationEntityRepository.findByFileID(TestUtils.FILE_ID);
    assertEquals(fileLocationEntityExpected.getFileID(),fileLocationEntityActual.getFileID());
    verify(cosmosStore,times(1)).findItem(any(),any(),any(),
        any(),any(),any());
  }

  @Test
  public void saveWithFileLocationEntityWithNull() {
    Throwable exception = assertThrows(IllegalArgumentException.class,
        ()->{fileLocationEntityRepository.save(null);} );
  }

  @Test()
  public void saveWithFileLocationEntityWithNonNull() {
    FileLocationEntity fileLocationEntityExpected = new FileLocationEntity();
    fileLocationEntityExpected.setFileID(TestUtils.FILE_ID);
    FileLocationEntity fileLocationEntityActual = fileLocationEntityRepository.save(fileLocationEntityExpected);
    assertEquals(fileLocationEntityExpected.getFileID(),fileLocationEntityActual.getFileID());
    verify(cosmosStore,times(1)).upsertItem(any(),any(),any(),any(),any());
  }

}
