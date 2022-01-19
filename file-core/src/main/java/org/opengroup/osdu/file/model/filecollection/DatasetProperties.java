package org.opengroup.osdu.file.model.filecollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DatasetProperties {

  @JsonProperty("FileCollectionPath")
  @NotNull(message = "FileSourceInfo cannot be null")
  @Valid
  private String fileCollectionPath;
}
