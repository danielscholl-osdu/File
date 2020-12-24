// Copyright © 2020 Amazon Web Services
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

@ExtendWith(MockitoExtension.class)
public class S3LocationTest {

  @Test
  public void should_create_with_validS3URI() {
    String uri = "s3://bucket/key/file";
    S3Location fileLocation = new S3Location(uri);
    assertEquals(true, fileLocation.isValid);
    assertEquals("bucket", fileLocation.bucket);
    assertEquals( "key/file", fileLocation.key);
  }

  @Test
  public void should_beInValidState_with_validS3URIbutWithOutKeyPath() {
    String uri = "s3://bucket";
    S3Location fileLocation = new S3Location(uri);
    assertEquals(false, fileLocation.isValid);
  }

  @Test
  public void should_beInValidState_with_emptyString() {
    String uri = "";
    S3Location fileLocation = new S3Location(uri);
    assertEquals(false, fileLocation.isValid);
  }

  @Test
  public void should_beInValidState_with_null() {
    String uri = null;
    S3Location fileLocation = new S3Location(uri);
    assertEquals(false, fileLocation.isValid);
  }
}
