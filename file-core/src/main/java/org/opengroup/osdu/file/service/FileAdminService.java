package org.opengroup.osdu.file.service;


import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileAdminService implements IFileAdminService {

  final DpsHeaders headers;
  final JaxRsDpsLog log;
  final IStorageService storageService;

    @Override
    public void revokeUrl(Map<String, String> revokeURLRequest) {
      boolean result = storageService.revokeUrl(revokeURLRequest);
      log.info("Result of revoke urls is " + result);
    }
}
