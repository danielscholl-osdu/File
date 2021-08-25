/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import org.opengroup.osdu.file.util.ExpiryTimeUtil;

import java.util.Date;

// Needed to add this class to make it easy to unit test the creation of a date
// to the current time of running
public class ExpirationDateHelper {
  public Date getExpirationDate(int s3SignedUrlExpirationTimeInDays){
    Date expiration = new java.util.Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * s3SignedUrlExpirationTimeInDays;
    expiration.setTime(expTimeMillis);
    return expiration;
  }

  public Date getExpirationTime(String expiryTimeInput){

    Date currentTime = new java.util.Date();
    ExpiryTimeUtil expiryTimeUtil = new ExpiryTimeUtil();

    ExpiryTimeUtil.RelativeTimeValue relativeExpiryTime = expiryTimeUtil
        .getExpiryTimeValueInTimeUnit(expiryTimeInput);

    long expTimeMillis = currentTime.getTime();

    switch (relativeExpiryTime.getTimeUnit()) {
    case MINUTES:
      expTimeMillis += 1000 * 60 * relativeExpiryTime.getValue();
      break;
    case HOURS:
      expTimeMillis += 1000 * 60 * 60 * relativeExpiryTime.getValue();
      break;
    case DAYS:
      expTimeMillis += 1000 * 60 * 60 * 24 * relativeExpiryTime.getValue();
      break;
    default:
      break;
    }

    return new Date(expTimeMillis);
  }

}
