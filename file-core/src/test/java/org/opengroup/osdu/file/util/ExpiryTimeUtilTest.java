package org.opengroup.osdu.file.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class ExpiryTimeUtilTest {

  private ExpiryTimeUtil expiryTimeUtil;
  private ExpiryTimeUtil.RelativeTimeValue relativeTimeValue;
  public static final String INVALID_EXPIRY_TIME_PATTERN = "expiryTime pattern isn't supported. Value should be one of these regex patterns ^[0-9]+M$ , ^[0-9]+H$ , ^[0-9]+D$";

  @BeforeEach
  void setUp() {
    expiryTimeUtil = new ExpiryTimeUtil();
  }

  @Test
  public void testExpiryTimeForMinutesInputString(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("3M");
    assertEquals(3L,relativeTimeValue.getValue());
    assertEquals(TimeUnit.MINUTES,relativeTimeValue.getTimeUnit());
  }

  @Test
  public void testExpiryTimeForHoursInputString(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("42H");
    assertEquals(42L,relativeTimeValue.getValue());
    assertEquals(TimeUnit.HOURS,relativeTimeValue.getTimeUnit());
  }

  @Test
  public void testExpiryTimeForDaysInputString(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("4D");
    assertEquals(4L,relativeTimeValue.getValue());
    assertEquals(TimeUnit.DAYS,relativeTimeValue.getTimeUnit());
  }

  @Test
  public void testExpiryTimeEqualToCappedDefaultValue(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("7D");
    verifyCappedDefaultExpiryTime(relativeTimeValue);
  }

  @Test
  public void testExpiryTimeLessThanCappedDefaultValue(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("6D");
    assertEquals(6L,relativeTimeValue.getValue());
    assertEquals(TimeUnit.DAYS,relativeTimeValue.getTimeUnit());

    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("120H");
    assertEquals(120L,relativeTimeValue.getValue());
    assertEquals(TimeUnit.HOURS,relativeTimeValue.getTimeUnit());

  }

  @Test
  public void testExpiryTimeGreaterThanCappedDefaultValue(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("9D");
    verifyCappedDefaultExpiryTime(relativeTimeValue);

    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("200H");
    verifyCappedDefaultExpiryTime(relativeTimeValue);

  }

  @Test
  public void testExpiryTimeUnmatchedInputPatterns(){
    Throwable thrown = catchThrowable(() -> expiryTimeUtil.getExpiryTimeValueInTimeUnit("24QD"));
    then(thrown).isInstanceOf(OsduBadRequestException.class);
    then(thrown).hasMessage(INVALID_EXPIRY_TIME_PATTERN);

    thrown = catchThrowable(() -> expiryTimeUtil.getExpiryTimeValueInTimeUnit("2d"));
    then(thrown).isInstanceOf(OsduBadRequestException.class);
    then(thrown).hasMessage(INVALID_EXPIRY_TIME_PATTERN);

  }

  @Test
  public void testExpiryTimeNullInput(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit(null);
    verifyDefaultExpiryTime(relativeTimeValue);
  }

  private void verifyDefaultExpiryTime(ExpiryTimeUtil.RelativeTimeValue relativeTimeValue) {
    assertEquals(1L, relativeTimeValue.getValue());
    assertEquals(TimeUnit.HOURS, relativeTimeValue.getTimeUnit());
  }

  private void verifyCappedDefaultExpiryTime(ExpiryTimeUtil.RelativeTimeValue relativeTimeValue) {
    assertEquals(7L, relativeTimeValue.getValue());
    assertEquals(TimeUnit.DAYS, relativeTimeValue.getTimeUnit());
  }

}
