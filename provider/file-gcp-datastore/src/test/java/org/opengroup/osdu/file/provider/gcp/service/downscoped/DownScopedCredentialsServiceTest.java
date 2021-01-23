package org.opengroup.osdu.file.provider.gcp.service.downscoped;

import com.google.auth.oauth2.ServiceAccountCredentials;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// import org.powermock.reflect.Whitebox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DownScopedCredentialsServiceTest {

    @Mock
    DownScopedOptions downScopedOptions;

    @Mock
    ServiceAccountCredentials serviceAccountCredentials;

    @InjectMocks
    DownScopedCredentialsService downScopedCredentialsService;

    @Test
    public void givenService_whenRequestDownscopedCredentials_thenCreatedWithProperArgs() {

        DownScopedCredentials dsc = downScopedCredentialsService.getDownScopedCredentials(serviceAccountCredentials, downScopedOptions);
        verify(serviceAccountCredentials).createScopedRequired();
        verify(serviceAccountCredentials).createScoped(anyCollectionOf(String.class));
        // assertEquals(Whitebox.getInternalState(dsc, "downScopedOptions"), downScopedOptions);
    }
}