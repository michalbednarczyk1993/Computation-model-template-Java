package com.setcom.computation.apiaccess;

import com.setcom.computation.datamodel.OutputTokenMessage;
import com.setcom.computation.datamodel.TokensAck;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.util.List;

@Slf4j
public class TokensProxy {
    private final CloseableHttpClient httpClient;
    private final String batchManagerAckUrl;
    private final String batchManagerTokenUrl;
    private final String senderUid;


    public TokensProxy()
    {
        httpClient = HttpClientBuilder.create().build();
        senderUid = System.getenv("SYS_MODULE_INSTANCE_UID");
        batchManagerAckUrl = System.getenv("SYS_BATCH_MANAGER_ACK_ENDPOINT");
        batchManagerTokenUrl = System.getenv("SYS_BATCH_MANAGER_TOKEN_ENDPOINT");

    }

    public StatusLine SendOutputToken(String pinName, String values, String baseMsgUid, boolean isFinal) {
        var xOutputToken = new OutputTokenMessage(pinName, senderUid, values, baseMsgUid, isFinal);
        HttpPost postRequest = new HttpPost(batchManagerTokenUrl);
        StatusLine status;

        try {
            var serializedXOutputToken = new StringEntity(new Gson().toJson(xOutputToken));

            postRequest.addHeader("content-type", "appliaction/json");
            postRequest.setEntity(serializedXOutputToken);
            HttpResponse result = httpClient.execute(postRequest);
            status = result.getStatusLine();
        } catch (IOException e) {
            log.error(e.toString());
            status = new BasicStatusLine(
                    new ProtocolVersion("HTTP", 2, 0), 400, e.toString());
        }

        return status;
    }

    public StatusLine SendAckToken(List<String> msgUids, boolean isFinal, boolean isFailed, String note) {
        var ackToken = new TokensAck(msgUids, senderUid, note, isFailed, isFinal);
        HttpPost postRequest = new HttpPost(batchManagerAckUrl);
        StatusLine status;

        try {
            var serializedAckToken = new StringEntity(new Gson().toJson(ackToken));

            postRequest.addHeader("content-type", "application/json");
            postRequest.setEntity(serializedAckToken);
            HttpResponse result = httpClient.execute(postRequest);
            status = result.getStatusLine();

        } catch (IOException e) {
            log.error(e.toString());
            status = new BasicStatusLine(
                    new ProtocolVersion("HTTP", 2, 0), 400, e.toString());
        }

        return status;
    }

    public StatusLine SendAckToken(List<String> msgUids, boolean isFinal, String note)  {
        return SendAckToken(msgUids, isFinal, false, note);
    }

    public StatusLine SendAckToken(List<String> msgUids, boolean isFinal)  {
        return SendAckToken(msgUids, isFinal, false, "");
    }
}
