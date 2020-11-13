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

package org.wso2.carbon.webhook.event;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTUtils;
import org.wso2.carbon.webhook.secret.manager.WebHookSecretManager;
import org.wso2.carbon.webhook.stat.publisher.WebHookStatPublisher;
import org.wso2.carbon.webhook.throttle.processor.ThrottleProcessor;
import org.wso2.carbon.webhook.utils.WebHookUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.wso2.carbon.webhook.WebHookConstants.BACK_SLASH;
import static org.wso2.carbon.webhook.WebHookConstants.CLIENT_CALL_BACK_URL_QUERY_PARAM;
import static org.wso2.carbon.webhook.WebHookConstants.HUB_SIGNATURE_HEADER_NAME;

public class WebHookEventHandler extends AbstractMediator implements ManagedLifecycle {

    private ThrottleProcessor throttleProcessor;
    private WebHookStatPublisher statPublisher;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        throttleProcessor = new ThrottleProcessor();
        statPublisher = new WebHookStatPublisher("event");
    }

    public boolean mediate(MessageContext synCtx) {

        RESTUtils.populateQueryParamsToMessageContext(synCtx);
        String requestPath = RESTUtils.getFullRequestPath(synCtx);
        log.info("Request Path : " + requestPath);

        String[] urlSegments = requestPath.split(BACK_SLASH);

        // todo : consider tenancy here
        if (urlSegments.length < 4) {
            log.error("Invalid request with path " + requestPath);
            // handle error
        }

        String apiContext = urlSegments[1];
        String topicName = urlSegments[2];

        Map<String, String> transportHeaders = WebHookUtils.getHeaderMap(synCtx);

        //hub.secret validation
        WebHookSecretManager secretManager = null;
        try {
            secretManager = new WebHookSecretManager(WebHookSecretManager.getSecret(apiContext, topicName));
        } catch (Exception e) {
            log.error("Error ", e);
            // todo
        }
        String payload = synCtx.getEnvelope().getBody().getFirstElement().toString();
        log.info("Payload\n" + payload);
        boolean validSignature = secretManager.checkSignature(
                WebHookUtils.getHeader(transportHeaders, HUB_SIGNATURE_HEADER_NAME), payload);
        if (!validSignature) {
            log.error("signature is not valid");
            // handle error
        }

        String clientCallbackUrl = WebHookUtils.getQueryParam(synCtx, CLIENT_CALL_BACK_URL_QUERY_PARAM);
        log.info("Client call back url : " + clientCallbackUrl);
        try {
            // todo :  change to query param
            clientCallbackUrl = WebHookUtils.decodeURI(urlSegments[3]);
        } catch (UnsupportedEncodingException e) {
            log.error("Error in decoding call back url", e);
            // handle error
        }

        // handle throttling
        if (!throttleProcessor.canProcessEvent(apiContext, topicName, clientCallbackUrl)) {
            log.error("Cannot process this event");
            //handle error
        }

        // set the signature header
        String secretHeader = secretManager.getHMACHeader(apiContext, topicName, clientCallbackUrl);
        if (secretHeader != null) {
            WebHookUtils.setHeader(transportHeaders, HUB_SIGNATURE_HEADER_NAME, secretHeader);
        }

        // set To Header
        synCtx.setTo(new EndpointReference(clientCallbackUrl));

        // stat
        statPublisher.publishStat();
        return true;
    }

    //    @Override
    //    public boolean isContentAware() {
    //        return false;
    //    }

    @Override
    public void destroy() {

    }
}
