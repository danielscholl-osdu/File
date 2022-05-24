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

package org.opengroup.osdu.file.provider.aws.impl;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudStorageOperationImpl implements ICloudStorageOperation {

    @Override
    public String copyFile(String sourceFilePath, String toFile) throws OsduBadRequestException {
        // we are not currently copying files, but providing access from the upload location.
        return null;
    }

    @Override
    public Boolean deleteFile(String location) {
        // we are not currently deleting files as we dont differentiate between staging/persistent.
        return false;
    }
}
