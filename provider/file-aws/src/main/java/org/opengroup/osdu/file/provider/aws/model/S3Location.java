/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.file.provider.aws.model;

import com.amazonaws.util.StringUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class S3Location {

    private static final String UNSIGNED_URL_PREFIX = "s3://";

    @Getter
    private String bucket;

    @Getter
    private String key;

    @Getter
    private boolean isValid = false;

    public S3Location(String bucket, String key) {
        if (bucket.startsWith(UNSIGNED_URL_PREFIX)) {
            throw new IllegalArgumentException("Bucket name should stats with 's3://'");
        }

        this.bucket = bucket;
        this.key = key;
        this.isValid = true;
    }

    private S3Location(String uri) {
        if (uri != null && uri.startsWith(UNSIGNED_URL_PREFIX)) {
            String[] bucketAndKey = uri.substring(UNSIGNED_URL_PREFIX.length()).split("/", 2);

            this.uriSetup(bucketAndKey);
        }
    }

    private void uriSetup(String[] bucketAndKey) {
        if (bucketAndKey.length == 2) {
            this.bucket = bucketAndKey[0];
            this.key = bucketAndKey[1];
            this.isValid = true;
        }
    }

    public static S3Location of(String bucket, String key) {
        return new S3Location(bucket, key);
    }

    public static S3Location of(String uri) {
        return new S3Location(uri);
    }

    public static S3LocationBuilder newBuilder() {
        return new S3LocationBuilder();
    }

    public boolean isFolder() {
        return key.endsWith("/");
    }

    public boolean isFile() {
        return !isFolder();
    }

    @Override
    public String toString() {
        return isValid ? String.format("%s%s/%s", UNSIGNED_URL_PREFIX, bucket, key) : "";
    }

    public static class S3LocationBuilder {

        private final List<String> path = new ArrayList<>();
        private boolean isFile = false;

        public S3LocationBuilder withBucket(String bucketName) {
            final String prefixedName = bucketName.startsWith(UNSIGNED_URL_PREFIX)
                                        ? bucketName
                                        : UNSIGNED_URL_PREFIX + bucketName;
            if (this.path.isEmpty()) {
                this.path.add(prefixedName);
            } else {
                this.handlePrefixedName(prefixedName);
            }

            return this;
        }

        private void handlePrefixedName(String prefixedName) {
            if (!this.path.get(0).startsWith(UNSIGNED_URL_PREFIX)) {
                this.path.add(0, prefixedName);
            } else {
                this.path.set(0, prefixedName);
            }
        }

        public S3LocationBuilder withFolder(String folderName) {
            if (!StringUtils.isNullOrEmpty(folderName)) {
                this.path.add(folderName);
                this.isFile = false;
            }

            return this;
        }

        public S3LocationBuilder withFile(String fileName) {
            if (!StringUtils.isNullOrEmpty(fileName)) {
                this.path.add(fileName);
                this.isFile = true;
            }

            return this;
        }

        public S3Location build() {
            String locationPath = String.join("/", this.path);
            if (!this.isFile) {
                locationPath += "/";
            }
            return S3Location.of(locationPath);
        }
    }
}
