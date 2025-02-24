package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.file.exception.EnumValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Schema(description = "Endianness of binary value. Enumeration- \\BIG\\ \\LITTLE\\.  If absent applications will need to interpret from context indicators.")
public enum Endian {

  BIG("BIG"), LITTLE("LITTLE");

  private final String value;

  Endian(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  String getType() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static Endian create(@JsonProperty("Endian") String value) throws EnumValidationException {
    if (value == null) {
      throw new EnumValidationException(value, "Endian");
    }
    return Stream
        .of(values())
        .filter(e -> e.toString().equals(value))
        .findFirst()
        .orElseThrow(new EnumValidationException(value, "Endian"));
  }
}
