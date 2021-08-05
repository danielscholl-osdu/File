package org.opengroup.osdu.file.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

  @Getter @Setter @AllArgsConstructor
  public class TimeValue {
    private long value;
    private TimeUnit timeUnit;
  }

  public TimeValue getExpiryTimeValueInTimeUnit(String expiryTime){

    long value = 7L;
    TimeUnit timeUnit = TimeUnit.DAYS;

    if(null != expiryTime){

      Optional<TimeUnitEnum> OptionalForSupportedTimeUnit = getTimeUnitForInput(expiryTime);
      if(OptionalForSupportedTimeUnit.isPresent()){

        value = extractValueFromRegexMatchedString(expiryTime);
        switch (OptionalForSupportedTimeUnit.get().toString()) {
        case "MINUTES":
          timeUnit = TimeUnit.MINUTES;
          break;
        case  "HOURS":
          timeUnit = TimeUnit.HOURS;
          break;
        case "DAYS":
          timeUnit = TimeUnit.DAYS;
          break;
        default:
          timeUnit = TimeUnit.DAYS;
          value = 7L;
          break;
        }
      }
    }
    return new TimeValue(value,timeUnit);
  }

  public OffsetDateTime getExpiryTimeInOffsetDateTime(String expiryTime) {

    OffsetDateTime expiryTimeInTimeUnit = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7);

    if (null != expiryTime) {

      Optional<TimeUnitEnum> supportedTimeUnitOptional = getTimeUnitForInput(expiryTime);
      if (supportedTimeUnitOptional.isPresent()) {

        long value = extractValueFromRegexMatchedString(expiryTime);
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);

        switch (supportedTimeUnitOptional.get().toString()) {
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
      }
    }
    return expiryTimeInTimeUnit;
  }

  private Long extractValueFromRegexMatchedString(String expiryTime) {
    return Long.valueOf(expiryTime.substring(0, expiryTime.length() - 1));
  }

  private Optional<TimeUnitEnum> getTimeUnitForInput(String input){
    for(TimeUnitEnum timeUnitEnum: TimeUnitEnum.values()){
      if(timeUnitEnum.regexPattern.matcher(input).matches()){
        return Optional.of(timeUnitEnum);
      }
    }
    return Optional.empty();
  }

}
