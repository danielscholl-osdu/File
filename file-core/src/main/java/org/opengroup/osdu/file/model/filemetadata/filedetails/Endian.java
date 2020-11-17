package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.stream.Stream;

import org.opengroup.osdu.file.exception.EnumValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  @JsonCreator
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
