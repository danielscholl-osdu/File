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

package org.opengroup.osdu.file.provider.aws.impl;

import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.ILocationMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Primary
@Component
public class LocationMapperImpl implements ILocationMapper {

    private static final String SIGNED_URL_KEY = "SignedURL";
    private static final String FILE_KIND_SOURCE = "FileSource";

    @Override
    public LocationResponse buildLocationResponse(SignedUrl signedUrl, FileLocation fileLocation) {
        Map<String, String> location = new HashMap<>();
        location.put(SIGNED_URL_KEY, signedUrl.getUrl().toString());
        location.put(FILE_KIND_SOURCE, signedUrl.getFileSource());

        return LocationResponse.builder()
                               .fileID(fileLocation.getFileID())
                               .location(location)
                               .build();
    }
}
