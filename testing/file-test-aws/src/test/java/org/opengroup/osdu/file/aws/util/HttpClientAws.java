// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.aws.util;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import com.google.common.base.Strings;

import org.opengroup.osdu.core.aws.cognito.AWSCognitoClient;
import org.opengroup.osdu.file.HttpClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class HttpClientAws extends HttpClient {

    private AWSCognitoClient cognitoClient;

    public HttpClientAws() {
        cognitoClient = new AWSCognitoClient();
    }

    @Override
    public synchronized String getAccessToken() throws IOException {
        if (Strings.isNullOrEmpty(accessToken)) {
            accessToken = cognitoClient.getTokenForUserWithAccess();
        }
        
        return "Bearer " + accessToken;
    }

    @Override
    public synchronized String getNoDataAccessToken() throws IOException {
        if (Strings.isNullOrEmpty(noDataAccessToken)) {

            noDataAccessToken = createInvalidToken("baduser@example.com");
        }
        return "Bearer " + noDataAccessToken;
    }

    private static String createInvalidToken(String username) {

        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(2048);
                
            KeyPair kp = keyGenerator.genKeyPair();
            PublicKey publicKey = (PublicKey) kp.getPublic();
            PrivateKey privateKey = (PrivateKey) kp.getPrivate();
            
            
            String token = Jwts.builder()
                    .setSubject(username)
                    .setExpiration(new Date())                
                    .setIssuer("info@example.com")                    
                    // RS256 with privateKey
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
                    
            return token;
        }
        catch (NoSuchAlgorithmException ex) {            
            return null;
        }
    }

}
