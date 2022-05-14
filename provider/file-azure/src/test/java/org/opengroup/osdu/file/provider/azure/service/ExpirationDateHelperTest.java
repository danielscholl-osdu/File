package org.opengroup.osdu.file.provider.azure.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ExpirationDateHelperTest {

  int numberOfDays = 5;

  @Test
  public void testGetExpirationDate(){
    ExpirationDateHelper expirationDateHelper = new ExpirationDateHelper();
    Date actualDate = expirationDateHelper.getExpirationDate(numberOfDays);
    Date expectedDate = new Date();
    long expTimeMillis = expectedDate.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * numberOfDays;
    expectedDate.setTime(expTimeMillis);
    assertEquals(expectedDate,actualDate);
  }

}
