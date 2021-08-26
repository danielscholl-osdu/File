package org.opengroup.osdu.file.provider.ibm.model.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class S3Location {

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public String bucket;

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public String key;

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public boolean isValid = false;

  private static final String UNSIGNED_URL_PREFIX = "s3://";

  public S3Location(String uri) {
    if (uri != null && uri.startsWith(UNSIGNED_URL_PREFIX)) {
      String[] bucketAndKey = uri.substring(UNSIGNED_URL_PREFIX.length()).split("/", 2);
      if (bucketAndKey.length == 2) {
        bucket = bucketAndKey[0];
        key = bucketAndKey[1];
        isValid = true;
      }
    }
  }
}
