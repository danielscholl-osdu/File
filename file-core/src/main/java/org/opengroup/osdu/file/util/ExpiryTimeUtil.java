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
    MINUTES(Pattern.compile("^[0-9]+M$"), TimeUnit.MINUTES),
    HOURS(Pattern.compile("^[0-9]+H$"), TimeUnit.HOURS),
    DAYS(Pattern.compile("^[0-9]+D$"), TimeUnit.DAYS);

    private Pattern regexPattern;
    private TimeUnit timeUnit;

    private TimeUnitEnum(Pattern pattern, TimeUnit timeUnit) {

      this.regexPattern = pattern;
      this.timeUnit = timeUnit;
    }

  }

  @Getter @Setter @AllArgsConstructor
  public class RelativeTimeValue {
    private long value;
    private TimeUnit timeUnit;
  }

  public RelativeTimeValue getExpiryTimeValueInTimeUnit(String input) {

    RelativeTimeValue defaultExpiryTime = new RelativeTimeValue(7L, TimeUnit.DAYS);
    Optional<TimeUnitEnum> OptionalForSupportedTimeUnit = getTimeUnitForInput(input);
    if (OptionalForSupportedTimeUnit.isPresent()) {
      long value = extractValueFromRegexMatchedString(input);
      RelativeTimeValue expiryTimeInTimeUnit = new RelativeTimeValue(value,
          OptionalForSupportedTimeUnit.get().timeUnit);

      if (getOffsetDateTimeFromNow(expiryTimeInTimeUnit)
          .compareTo(getOffsetDateTimeFromNow(defaultExpiryTime)) <= 0)
        return expiryTimeInTimeUnit;

    }

    return defaultExpiryTime;
  }

  public OffsetDateTime getExpiryTimeInOffsetDateTime(String expiryTime) {

    RelativeTimeValue time = getExpiryTimeValueInTimeUnit(expiryTime);
    return getOffsetDateTimeFromNow(time);
  }

  private OffsetDateTime getOffsetDateTimeFromNow(RelativeTimeValue time) {
    OffsetDateTime offsetExpiryTime = OffsetDateTime.now(ZoneOffset.UTC);
    switch (time.timeUnit) {
    case MINUTES:
      offsetExpiryTime = offsetExpiryTime.plusMinutes(time.value);
      break;
    case HOURS:
      offsetExpiryTime = offsetExpiryTime.plusHours(time.value);
      break;
    case DAYS:
      offsetExpiryTime = offsetExpiryTime.plusDays(time.value);
      break;
    default:
      break;
    }
    return offsetExpiryTime;
  }

  private Long extractValueFromRegexMatchedString(String expiryTime) {
    return Long.valueOf(expiryTime.substring(0, expiryTime.length() - 1));
  }

  private Optional<TimeUnitEnum> getTimeUnitForInput(String input) {
    if (null != input) {
      for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
        if (timeUnitEnum.regexPattern.matcher(input).matches()) {
          return Optional.of(timeUnitEnum);
        }
      }
    }
    return Optional.empty();
  }

}
