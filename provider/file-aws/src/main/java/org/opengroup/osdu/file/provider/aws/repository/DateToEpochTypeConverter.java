package org.opengroup.osdu.file.provider.aws.repository;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class DateToEpochTypeConverter implements DynamoDBTypeConverter<Long, Date> {

    @Override
    public Long convert(Date date) {
        return date.getTime();
    }

    @Override
    public Date unconvert(Long l) {
        return new Date(l);
    }
}