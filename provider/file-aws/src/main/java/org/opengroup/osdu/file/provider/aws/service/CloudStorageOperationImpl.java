package org.opengroup.osdu.file.provider.aws.service;

import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudStorageOperationImpl implements ICloudStorageOperation {

    @Override
    public String copyFile(String sourceFilePath, String toFile) throws OsduBadRequestException {
        
        //we are not currently copying files, but providing access from the upload location
        return null;
    }

    @Override
    public Boolean deleteFile(String location) {
        //we are not currently deleting files as we dont differentiate between staging/persistent
        return false;
    }
    
}
