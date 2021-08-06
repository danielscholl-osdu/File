package org.opengroup.osdu.file.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class ExpiryTimeUtilTest {

  private ExpiryTimeUtil expiryTimeUtil;
  private ExpiryTimeUtil.RelativeTimeValue relativeTimeValue;

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
  public void testExpiryTimeGreaterThanCappedDefaultValue(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("9D");
    verifyDefaultExpirtyTime(relativeTimeValue);

    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("200H");
    verifyDefaultExpirtyTime(relativeTimeValue);

  }

  @Test
  public void testExpiryTimeUnmatchedInputPatterns(){
    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("2d");
    verifyDefaultExpirtyTime(relativeTimeValue);

    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit("24QD");
    verifyDefaultExpirtyTime(relativeTimeValue);

    relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit(null);
    verifyDefaultExpirtyTime(relativeTimeValue);

  }


  private void verifyDefaultExpirtyTime(ExpiryTimeUtil.RelativeTimeValue relativeTimeValue) {
    assertEquals(7L, relativeTimeValue.getValue());
    assertEquals(TimeUnit.DAYS, relativeTimeValue.getTimeUnit());
  }

}
