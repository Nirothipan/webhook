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

package org.wso2.carbon.webhook.utils;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebHookUtils {

    public static Map<String, String> getHeaderMap(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        return (Map) msgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    public static String getHeader(Map header, String headerName) {

        if (header != null) {
            Object headerObject = header.get(headerName);
            if (headerObject != null) {
                return (String) headerObject;
            }
        }
        return null;
    }

    public static void setHeader(Map header, String headerName, String headerValue) {

        header.put(headerName, headerValue);
    }

    public static String getQueryParam(MessageContext synCtx, String paramName){

        return (String) synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + paramName);
    }

    public static String encodeURI(String url) throws UnsupportedEncodingException {
        return  URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
    }
}
