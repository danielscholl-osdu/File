/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.SignUrlOption;
import org.apache.http.HttpStatus;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.provider.gcp.config.properties.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.*;
// import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties({GcpConfigurationProperties.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class DeliveryStorageServiceImplTest {

    @Autowired
    @Spy
    GcpConfigurationProperties gcpConfigurationProperties;

    @Mock
    private InstantHelper instantHelper;

    @Mock
    private Storage storage;

    @Mock
    private StorageOptions storageOptions;

    @Mock
    private ServiceAccountCredentials serviceAccountCredentials;

    @Mock
    private DownScopedCredentialsService downScopedCredentialsService;

    @Mock
    private DownScopedCredentials downScopedCredentials;

    @Mock
    private AccessToken downScopedToken;

    @Mock
    private Blob blob;

    private String downScopedTokenValue = "connectionString";

    @InjectMocks
    private DeliveryStorageServiceImpl storageService;

    private String bucketName = "osdu-sample-osdu-file";
    private String key = "/common-user/1590050272122-2020-05-21-08-37-52-122/cebdd5780fc74f24b518c9676160136f";
    private String unsignedUrl = "gs://" + bucketName + "/" + key;

    @BeforeEach
    public void before() throws IOException {
        lenient().when(storageOptions.getCredentials()).thenReturn(serviceAccountCredentials);
        lenient().when(storage.getOptions()).thenReturn(storageOptions);
        lenient().when(downScopedCredentialsService.getDownScopedCredentials(any(DownScopedOptions.class))).thenReturn(downScopedCredentials);
        lenient().when(downScopedCredentials.refreshAccessToken()).thenReturn(downScopedToken);
        lenient().when(downScopedToken.getTokenValue()).thenReturn(downScopedTokenValue);

    }

    @Test
    public void givenBlobResource_whenCreateSignedUrl_thenCreatedProperly() throws IOException, URISyntaxException {

        URL url = new URL("http://testsignedurl.com");

        lenient().when(storage.get(any(BlobId.class))).thenReturn(blob);
        lenient().when(storage
                .signUrl(any(BlobInfo.class),
                        any(Long.class),
                        any(TimeUnit.class),
                        any(SignUrlOption.class),
                        any(SignUrlOption.class)))
                .thenReturn(url);

        Instant instant = Instant.now();
        Mockito.when(instantHelper.getCurrentInstant()).thenReturn(instant);

        SignedUrl expected = new SignedUrl();
        expected.setUri(new URI(url.toString()));
        expected.setUrl(url);
        expected.setCreatedAt(instant);

        SignedUrl actual = storageService.createSignedUrl(unsignedUrl, null);

        assertEquals(expected, actual);
    }

    @Test
    public void givenFolderResource_whenCreateSignedUrl_thenCreatedProperly() throws IOException {

        URL url = new URL("http://testsignedurl.com");

        when(storage.get(any(BlobId.class))).thenReturn(null);

        Instant instant = Instant.now();
        when(instantHelper.getCurrentInstant()).thenReturn(instant);

        SignedUrl expected = new SignedUrl();
        expected.setCreatedAt(instant);
        expected.setConnectionString(downScopedTokenValue);

        SignedUrl actual = storageService.createSignedUrl(unsignedUrl, null);

        assertEquals(expected, actual);
    }

    @Test
    public void createSignedUrl_malformedUnsignedUrl_throwsAppException() {
        try {
            String unsignedUrl = "malformedUrlString";
            String authorizationToken = "testAuthorizationToken";

            storageService.createSignedUrl(unsignedUrl, authorizationToken);

            fail("Should not succeed!");
        } catch (AppException e) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getError().getCode());
            assertEquals("Malformed URL", e.getError().getReason());
            assertEquals("Unsigned url invalid, needs to be full GS path", e.getError().getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }
}
