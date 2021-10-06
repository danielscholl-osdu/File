package org.opengroup.osdu.file.provider.reference.repository;

import io.minio.CopyObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import java.io.InputStream;

public interface MinioRepository {

  InputStream getFile(GetObjectArgs args);

  Iterable<Result<Item>> listObjects(ListObjectsArgs args);

  String getSignedUrl(GetPresignedObjectUrlArgs args);

  ObjectWriteResponse copyFile(CopyObjectArgs args);

  void deleteFile(RemoveObjectArgs args);
}
