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

package org.opengroup.osdu.file.provider.aws.model.constant;

public final class StorageConstant {

    private StorageConstant() {
        //private constructor
    }

    // https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-keys.html
    public static final int AWS_MAX_KEY_LENGTH = 1_024;
    
    public static final int AWS_HASH_BYTE_ARRAY_LENGTH = 1024;
}
