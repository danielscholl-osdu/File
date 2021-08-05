package org.opengroup.osdu.file.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignedUrlParameters {
  private String expiryTime;
}
