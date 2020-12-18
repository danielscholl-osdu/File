package org.opengroup.osdu.file.provider.aws.service;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudStorageOperationImpl implements ICloudStorageOperation {

    @Override
    public String copyFile(String sourceFilePath, String toFile) throws OsduBadRequestException {
        throw new NotImplementedException("Copy File Not Yet Implemented...");
    }

    @Override
    public Boolean deleteFile(String location) {
        throw new NotImplementedException("Delete File Not Yet Implemented...");
    }
    
}
