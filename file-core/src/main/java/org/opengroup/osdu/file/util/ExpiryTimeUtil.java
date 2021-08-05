package org.opengroup.osdu.file.util;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class ExpiryTimeUtil {

  private enum TimeUnitEnum {
    MINUTES(Pattern.compile("^[0-9]+M$")),
    HOURS(Pattern.compile("^[0-9]+H$")),
    DAYS(Pattern.compile("^[0-9]+D$"));

    private Pattern regexPattern;
    private TimeUnitEnum(Pattern pattern){
      this.regexPattern = pattern;
    }

  }

  public OffsetDateTime getExpiryTimeInTimeUnit(String expiryTime){

    OffsetDateTime expiryTimeInTimeUnit = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7);

    if (null != expiryTime) {

      for(TimeUnitEnum timeUnit: TimeUnitEnum.values()){
        if(timeUnit.regexPattern.matcher(expiryTime).matches()){

          long value = Long.valueOf(expiryTime.substring(0, expiryTime.length() - 1));
          OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);

          switch (timeUnit.toString()) {
          case "MINUTES":
            expiryTimeInTimeUnit = currentTime.plusMinutes(value);
            break;
          case "HOURS":
            expiryTimeInTimeUnit = currentTime.plusHours(value);
            break;
          case "DAYS":
            expiryTimeInTimeUnit = currentTime.plusDays(value);
            break;
          default:
            break;
          }

          break;
        }
      }
    }
    return  expiryTimeInTimeUnit;
  }

}
