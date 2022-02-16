package org.opengroup.osdu.file.service;

import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageUtilService;
import org.springframework.stereotype.Component;

/**
 * There are dummy beans to avoid deployment failure.
 * Respective provides can implement this while implementing file collection.
 */
@Component
public class FileCollectionStorageUtilService implements IFileCollectionStorageUtilService {
}
