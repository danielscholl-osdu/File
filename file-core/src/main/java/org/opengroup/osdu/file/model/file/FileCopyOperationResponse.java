package org.opengroup.osdu.file.model.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCopyOperationResponse {
  private FileCopyOperation copyOperation;
  private boolean success;
}
