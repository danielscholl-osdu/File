package org.opengroup.osdu.file.provider.gcp.util;

import com.google.cloud.storage.Acl;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleCloudStorageUtil {

    public List<Acl> getAcls(String serviceAccount) {
        List<Acl> acls = new ArrayList<>();
        acls.add(Acl.newBuilder(new Acl.User(serviceAccount), Acl.Role.OWNER).build());
        return acls;
    }



    public String getCompleteFilePath(String destinationBucket, String filePath) {
        StringBuilder destinationBlob = new StringBuilder();
        destinationBlob.append("gs://").append(destinationBucket).append(filePath);
        return destinationBlob.toString();
    }


    public String getPersistentBucket(String tenantProject) {
        return tenantProject + "-" + FileMetadataConstant.PERSISTENT_AREA_EXT;
    }

    public String getStagingBucket(String tenantProject) {
        return tenantProject + "-" + FileMetadataConstant.STAGING_AREA_EXT;
    }


    public boolean isPathsEmpty(String fromBucket, String fromPath, String toBucket, String toPath) {
        return StringUtils.isEmpty(fromBucket) || StringUtils.isEmpty(fromPath) || StringUtils.isEmpty(toBucket) || StringUtils.isEmpty(toPath);
    }

    public String getBucketName(String filePath) {

        String[] filePathChunks = filePath.split(FileMetadataConstant.FORWARD_SLASH);
        String sourceBucket = "";

        if (filePathChunks.length > 1) {
            sourceBucket = filePathChunks[2];
        }

        return sourceBucket;
    }

    public String getFolderName(String filePath) {
       return filePath.split(FileMetadataConstant.FORWARD_SLASH)[0] + FileMetadataConstant.FORWARD_SLASH;
    }

    public String getDirectoryPath(String filePath) {

        String result = "";
        String bucketName = getBucketName(filePath);

        int initialIndex = filePath.indexOf(bucketName) + bucketName.length() + 1;
        int lastIndex = filePath.length();
        if ((lastIndex > 0) && (lastIndex > initialIndex)) {
            result = filePath.substring(initialIndex, lastIndex);
        }
        return result;
    }
}
