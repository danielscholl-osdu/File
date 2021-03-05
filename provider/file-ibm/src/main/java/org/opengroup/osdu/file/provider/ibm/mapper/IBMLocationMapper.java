/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.mapper;

import java.util.HashMap;
import java.util.Map;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.ILocationMapper;
import org.springframework.stereotype.Component;

@Component
public class IBMLocationMapper implements ILocationMapper {

  private static final String SIGNED_URL_KEY = "SignedURL";
  private static final String FILE_SOURCE_KEY = "FileSource";

  @Override
  public LocationResponse buildLocationResponse(SignedUrl signedUrl, FileLocation fileLocation) {
    Map<String, String> location = new HashMap<>();
    location.put(SIGNED_URL_KEY, signedUrl.getUrl().toString());
    location.put(FILE_SOURCE_KEY, signedUrl.getFileSource());
    return LocationResponse.builder()
        .fileID(fileLocation.getFileID())
        .location(location)
        .build();
  }
}
