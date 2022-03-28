package com.setcom.computation.apiaccess;


import com.setcom.computation.datamodel.TokensAck;

import java.net.http.HttpClient;
import java.util.List;

public class TokensProxy {
    private final HttpClient httpClient;
    private final String batchManagerAckUrl;
    private final String batchManagerTokenUrl;
    private final String senderUid;

    public TokensProxy()
    {
        httpClient = new HttpClient();
        senderUid = System.getenv("SYS_MODULE_INSTANCE_UID");
        batchManagerAckUrl = System.getenv("SYS_BATCH_MANAGER_ACK_ENDPOINT");
        batchManagerTokenUrl = System.getenv("SYS_BATCH_MANAGER_TOKEN_ENDPOINT");
    }

    public HttpStatusCode SendOutputToken(String pinName, String values, String baseMsgUid, boolean isFinal)
    {
        var xOutputToken = new OutputTokenMessage
        {
            PinName = pinName,
                    SenderUid = _senderUid,
                    Values = values,
                    BaseMsgUid = baseMsgUid,
                    IsFinal = isFinal
        };

        var serializedXOutputToken = JsonConvert.SerializeObject(xOutputToken);
        var data = new StringContent(serializedXOutputToken, Encoding.UTF8, "application/json");
        var result = httpClient.PostAsync(_batchManagerTokenUrl, data).Result.StatusCode;

        return result;
    }

    public HttpStatusCode SendAckToken(List<String> msgUids, boolean isFinal, String note)  {
        return SendAckToken(msgUids, isFinal, false, note);
    }

    public HttpStatusCode SendAckToken(List<String> msgUids, boolean isFinal, boolean isFailed, String note)
    {
        var ackToken = new TokensAck
        {
            SenderUid = _senderUid,
                    MsgUids = msgUids,
                    IsFinal = isFinal,
                    IsFailed = isFailed,
                    Note = note
        };

        var serializedAckToken = JsonConvert.SerializeObject(ackToken);
        var data = new StringContent(serializedAckToken, Encoding.UTF8, "application/json");

        var result = httpClient.PostAsync(batchManagerAckUrl, data).Result.StatusCode;

        return result;
    }
}
