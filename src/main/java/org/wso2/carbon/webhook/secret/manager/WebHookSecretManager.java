/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webhook.secret.manager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class WebHookSecretManager {

    private static final Log log = LogFactory.getLog(WebHookSecretManager.class);

    private static final String HMAC_SHA1 = "HmacSHA1";
    private final Mac mac;

    public WebHookSecretManager(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        mac = Mac.getInstance(HMAC_SHA1);
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
        mac.init(signingKey);
    }

    public String getHMACHeader(String apiName, String topicName, String clientCallBackUrl) {

        // get secret and if is not null generate and return header, else just return null
        return "secret";
    }

    public static String getSecret(String apiName, String topicName) {

        // get secret and if is not null generate and return header, else just return null
        return "secret";
    }

    /**
     * Checks a github signature against its payload
     *
     * @param signature A X-Hub-Signature header value ("sha1=[...]")
     * @param payload   The signed HTTP request body
     * @return Whether the signature is correct for the checker's secret
     */
    public boolean checkSignature(String signature, String payload) {

        if (signature == null || signature.length() != 45) {
            return false;
        }
        final char[] hash = Hex.encodeHex(this.mac.doFinal(payload.getBytes()));
        final StringBuilder builder = new StringBuilder("sha1=");
        builder.append(hash);
        final String expected = builder.toString();
        return expected.equals(signature);
    }

}
