// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

@ExtendWith(MockitoExtension.class)
public class S3LocationTest {

    @Test
    public void shouldCreateWithValidS3Uri() {
        String uri = "s3://bucket/key/file";
        S3Location fileLocation = S3Location.of(uri);

        assertTrue(fileLocation.isValid());
        assertEquals("bucket", fileLocation.getBucket());
        assertEquals("key/file", fileLocation.getKey());
    }

    @Test
    public void shouldBeInValidStateWithValidS3UriButWithOutKeyPath() {
        String uri = "s3://bucket";
        S3Location fileLocation = S3Location.of(uri);

        assertFalse(fileLocation.isValid());
    }

    @Test
    public void shouldBeInValidStateWithEmptyString() {
        String uri = "";
        S3Location fileLocation = S3Location.of(uri);

        assertFalse(fileLocation.isValid());
    }

    @Test
    public void shouldBeInValidStateWithNull() {
        String uri = null;

        S3Location fileLocation = S3Location.of(uri);
        assertFalse(fileLocation.isValid());
    }

    @Test
    public void shouldGetCorrectString() {
        assertEquals("s3://bucket/key/file", S3Location.of("s3://bucket/key/file").toString());

        assertEquals("", S3Location.of("").toString());
    }

    @Test
    public void shouldHandleBucketWithPrefixAnWithoutPrefix() {
        S3Location locationForBucketWithoutPrefix = S3Location.newBuilder().withBucket("abc")
                                                              .withFolder("def")
                                                              .build();

        assertEquals("s3://abc/def/", locationForBucketWithoutPrefix.toString());

        S3Location locationForBucketWithPrefix = S3Location.newBuilder().withBucket("s3://abc")
                                                           .withFolder("def")
                                                           .build();

        assertEquals("s3://abc/def/", locationForBucketWithPrefix.toString());
    }

    @Test
    public void shouldBuildFile() {
        S3Location location = S3Location.newBuilder().withBucket("abc")
                                        .withFolder("def")
                                        .withFile("123.txt")
                                        .withFolder("456")
                                        .withFile("789.txt")
                                        .build();

        assertTrue(!location.isFolder());
        assertEquals("s3://abc/def/123.txt/456/789.txt", location.toString());
    }

    @Test
    public void shouldBuildFolder() {
        S3Location location = S3Location.newBuilder().withBucket("abc")
                                        .withFolder("def")
                                        .withFolder("123")
                                        .build();

        assertTrue(location.isFolder());
        assertEquals("s3://abc/def/123/", location.toString());
    }
}
