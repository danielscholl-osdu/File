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

package org.opengroup.osdu.file.provider.aws.model;

import lombok.Getter;

public class S3Location {

    private static final String UNSIGNED_URL_PREFIX = "s3://";

    @Getter
    private String bucket;

    @Getter
    private String key;

    @Getter
    private boolean isValid = false;

    private S3Location(String uri) {
        if (uri != null && uri.startsWith(UNSIGNED_URL_PREFIX)) {
            String[] bucketAndKey = uri.substring(UNSIGNED_URL_PREFIX.length()).split("/", 2);

            if (bucketAndKey.length == 2) {
                bucket = bucketAndKey[0];
                key = bucketAndKey[1];
                isValid = true;
            }
        }
    }

    public static S3Location of(String uri) {
        return new S3Location(uri);
    }

    @Override
    public String toString() {
        return isValid ? String.format("%s%s/%s", UNSIGNED_URL_PREFIX, bucket, key) : "";
    }
}
