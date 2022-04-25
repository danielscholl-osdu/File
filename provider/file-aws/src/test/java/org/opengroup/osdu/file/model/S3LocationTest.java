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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

@ExtendWith(MockitoExtension.class)
public class S3LocationTest {

    @Test
    public void should_create_with_valid_s_3_uri() {
        String uri = "s3://bucket/key/file";
        S3Location fileLocation = S3Location.of(uri);

        assertTrue(fileLocation.isValid());
        assertEquals("bucket", fileLocation.getBucket());
        assertEquals("key/file", fileLocation.getKey());
    }

    @Test
    public void should_be_in_valid_state_with_valid_s_3_uri_but_with_out_key_path() {
        String uri = "s3://bucket";
        S3Location fileLocation = S3Location.of(uri);

        assertFalse(fileLocation.isValid());
    }

    @Test
    public void should_be_in_valid_state_with_empty_string() {
        String uri = "";
        S3Location fileLocation = S3Location.of(uri);

        assertFalse(fileLocation.isValid());
    }

    @Test
    public void should_be_in_valid_state_with_null() {
        String uri = null;

        S3Location fileLocation = S3Location.of(uri);
        assertFalse(fileLocation.isValid());
    }

    @Test
    public void should_get_correct_string() {
        assertEquals("s3://bucket/key/file", S3Location.of("s3://bucket/key/file").toString());
        assertEquals("", S3Location.of(null).toString());
    }
}
