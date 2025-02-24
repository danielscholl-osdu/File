package org.opengroup.osdu.file.service;

import java.util.Map;

public interface IFileAdminService {
  void revokeUrl(Map<String, String> revokeURLRequest);
}
