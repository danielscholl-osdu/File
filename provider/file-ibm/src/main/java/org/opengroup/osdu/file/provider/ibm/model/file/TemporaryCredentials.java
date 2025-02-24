package org.opengroup.osdu.file.provider.ibm.model.file;

import com.amazonaws.auth.AWSSessionCredentials;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryCredentials implements AWSSessionCredentials {

    private static String CONN_STRING_FORMAT =
            "AccessKeyId=%s;SecretAccessKey=%s;SessionToken=%s;Expiration=%s";

    @JsonProperty("accessKeyId")
    private String accessKeyId;

    @JsonProperty("secretAccessKey")
    private String secretAccessKey;

    @JsonProperty("sessionToken")
    private String sessionToken;

    @JsonProperty("expiration")
    private Date expiration;

    public String toConnectionString() {
        if (accessKeyId == null || accessKeyId == "") {
            return "";
        }
        String expirationString = DateTimeFormatter.ISO_INSTANT.format(expiration.toInstant());
        return String.format(CONN_STRING_FORMAT, accessKeyId, secretAccessKey, sessionToken, expirationString);
    }

    @Override
    @JsonIgnore
    public String getAWSAccessKeyId() {
        return accessKeyId;
    }

    @Override
    @JsonIgnore
    public String getAWSSecretKey() {
        return secretAccessKey;
    }

    @Override
    @JsonIgnore
    public String getSessionToken() {
        return sessionToken;
    }

}
