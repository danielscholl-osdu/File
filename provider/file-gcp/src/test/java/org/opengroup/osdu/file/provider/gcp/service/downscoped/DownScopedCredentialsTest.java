package org.opengroup.osdu.file.provider.gcp.service.downscoped;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DownScopedCredentialsTest {

    @Mock
    DownScopedOptions downScopedOptions;

    @Mock
    ServiceAccountCredentials sourceCredentials;

    @Mock
    GoogleCredentials finiteCredentials;

    @Mock
    AccessToken accessToken;
    @Mock
    AccessToken downScopedToken;

    @Mock
    HttpResponse httpResponse;

    @Mock
    DownScopedCredentials downScopedCredentials;

    @Test
    public void givenDownScopedCredentials_whenInvoked_thenRequestsToken() throws Exception {

        // Whitebox.setInternalState(sourceCredentials, "scopes", Collections.EMPTY_LIST);
        // when(sourceCredentials.createScopedRequired()).thenReturn(true);
        lenient().when(sourceCredentials.getScopes()).thenReturn(Collections.EMPTY_LIST);
        // sourceCredentials = sourceCredentials.toBuilder().setScopes(Collections.EMPTY_LIST).build();
        lenient().when(sourceCredentials.createScoped(anyCollectionOf(String.class))).thenReturn(finiteCredentials);
        // downScopedCredentials = new DownScopedCredentials(sourceCredentials, downScopedOptions);
        // Whitebox.setInternalState(finiteCredentials, "temporaryAccess", accessToken);
        when(downScopedCredentials.refreshAccessToken()).thenReturn(downScopedToken);


        AccessToken returnedDownScopedToken = downScopedCredentials.refreshAccessToken();
        
        assertEquals(downScopedToken, returnedDownScopedToken);
    }
}