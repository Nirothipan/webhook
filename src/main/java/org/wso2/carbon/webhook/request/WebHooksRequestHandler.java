package org.wso2.carbon.webhook.request;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.webhook.stat.publisher.WebhooksStatPublisher;
import org.wso2.carbon.webhook.utils.WebHookUtils;

import java.io.UnsupportedEncodingException;

public class WebHooksRequestHandler extends AbstractMediator implements ManagedLifecycle {

    WebhooksStatPublisher statPublisher;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        statPublisher = new WebhooksStatPublisher();
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        statPublisher.publishStat();

        addSecurityHeader(messageContext);

        String api = (String) ((Axis2MessageContext) messageContext).getProperty("SYNAPSE_REST_API");
        String callback = ((String)((Axis2MessageContext) messageContext).getProperty("callback"));
        String topic = ((String)((Axis2MessageContext) messageContext).getProperty("callback"));
        String encodedCallback = callback;
        String encodedTopic = topic;
        try {
            encodedCallback = WebHookUtils.encodeURI(callback);
            encodedTopic = WebHookUtils.encodeURI(topic);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
        String endpointURL = constructEndpointURL();
        String newCallBackURL = endpointURL + "/" + api + "/" + encodedTopic + "/" + encodedCallback;
        storeData();
        generateHubSecret();
        appendCallBackURL();

        return true;
    }

    @Override
    public void destroy() {

    }

    private String constructEndpointURL() {
        return "http://dbd50f077dda.ngrok.io";
    }

    private void appendCallBackURL() {

    }

    private void addSecurityHeader(MessageContext messageContext) {
        //add header of there any
    }

    private void generateHubSecret() {
        //generate hub secret and append
    }

    private void storeData() {
        //store callback url, hus secret etc
    }
}
