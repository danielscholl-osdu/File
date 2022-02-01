package org.opengroup.osdu.file.model.filecollection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.file.model.file.FileCopyOperation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetCopyOperation {
  private FileCopyOperation fileCopyOperation;
  private boolean success;
}
