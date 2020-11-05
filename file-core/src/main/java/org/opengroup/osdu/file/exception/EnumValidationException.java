package org.opengroup.osdu.file.exception;

import java.util.function.Supplier;

import lombok.Data;

@Data
public class EnumValidationException extends Exception
        implements Supplier<EnumValidationException> {

    private String enumValue = null;
    private String enumName = null;

    public EnumValidationException(String enumValue, String enumName) {
        super("Invalid value of " +  enumValue + " for " + enumName);
        this.enumValue = enumValue;
        this.enumName = enumName;
    }


    @Override
    public EnumValidationException get() {
        return new EnumValidationException(enumValue, enumName);
    }
}
