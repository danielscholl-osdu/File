package org.opengroup.osdu.file.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignedUrlParameters {
  private String expiryTime;
  private String fileName;
  private String contentType;

    public SignedUrlParameters(String expiryTime) {
        this.expiryTime = expiryTime;
    }
}
