package com.setcom.computation.balticlsc;

import com.google.gson.Gson;
import com.setcom.computation.apiaccess.TokensProxy;
import com.setcom.computation.dataaccess.MongoDbHandle;
import com.setcom.computation.datamodel.Status;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.javatuples.Pair;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataHandler implements IDataHandler{

    private final Map<String, DataHandle> dataHandles;
    private final TokensProxy tokensProxy;
    private final JobRegistry registry;
    private final JSONObject configuration;

    /**
     *
     * @param registry
     * @param configuration
     */
    public DataHandler(JobRegistry registry, JSONObject configuration) {
        this.registry = registry;
        this.tokensProxy = new TokensProxy();
        this.configuration = configuration;
        this.dataHandles = new HashMap<>();
    }

    /**
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    public String obtainDataItem(String pinName) throws Exception{
        Pair<List<String>, long[]> obtainData = obtainDataItemsNDim(pinName);
        if (null == obtainData.getValue0() || 0 == obtainData.getValue0().size())
            return null;
        if (null == obtainData.getValue1() && 1 == obtainData.getValue0().size())
            return obtainData.getValue0().get(0);
        throw new Exception("Improper call - more than one data item exists for the pin");
    }

    /**
     *
     * @param pinName
     * @return
     */
    public List<String> obtainDataItems(String pinName) throws Exception {
        Pair<List<String>, long[]> obtainData = obtainDataItemsNDim(pinName);
        if (null != obtainData.getValue1() && 1 == obtainData.getValue1().length)
        return obtainData.getValue0();
        throw new Exception("Improper call - more than one dimension exists for the pin");
    }

    /**
     *
     * @param pinName
     * @return
     */
    public Pair<List<String>, long[]> obtainDataItemsNDim(String pinName) {
        try {
            Pair<List<String>, long[]> pinValues = registry.getPinValuesNDim(pinName);
            List<Map<String,String>> valuesObject =
                    pinValues.getValue0().stream().map(v-> (v != null && !v.isEmpty()) ?
                            new Gson().fromJson(v, HashMap.class) : null).collect(Collectors.toList());
            DataHandle dHandle = GetDataHandle(pinName);
            List<String> dataItems = valuesObject.stream().map(v-> {
                        try {
                            return (null != v) ? dHandle.download(v) : null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).
                    collect(Collectors.toList());
            return new Pair<>(dataItems, pinValues.getValue1());
        } catch (IllegalArgumentException e) {
            return registry.getPinValuesNDim(pinName);
        }
    }

    /**
     *
     * @param pinName
     * @param data
     * @param isFinal
     * @param msgUid
     * @return
     */
    public short sendDataItem(String pinName, String data, boolean isFinal, @Nullable String msgUid) throws Exception {
        if (registry.getPinConfiguration(pinName).accessType.equals("Direct"))
            return sendToken(pinName, data, isFinal, msgUid);
        DataHandle dHandle = GetDataHandle(pinName);
        Map<String,String> newHandle = dHandle.upload(data);
        return sendToken(pinName, new Gson().toJson(newHandle), isFinal, msgUid);
    }

    /**
     *
     * @param pinName
     * @param values
     * @param isFinal
     * @param msgUid
     * @return
     */
    public short sendToken(String pinName, String values, boolean isFinal, @Nullable String msgUid) {
        if (null == msgUid)
            msgUid = registry.getBaseMsgUid();
        StatusLine status = new BasicStatusLine(
                new ProtocolVersion("HTTP", 2, 0), 200, "");
        return status.equals(tokensProxy.SendOutputToken(pinName, values, msgUid, isFinal)) ? (short)0 : (short)-1;
    }

    public short finishProcessing() {
        List<String> msgUids = registry.getAllMsgUids();
        registry.setStatus(Status.COMPLETED);
        return sendAckToken(msgUids, true);
    }

    public short FailProcessing(String note) {
        List<String> msgUids = registry.getAllMsgUids();
        registry.setStatus(Status.FAILED);
        StatusLine status = new BasicStatusLine(
                new ProtocolVersion("HTTP", 2, 0), 200, "");
        if (status.equals(tokensProxy.SendAckToken(msgUids, true, true, note))) {
            registry.clearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    /**
     *
     * @param msgUids
     * @param isFinal
     * @return
     */
    public short sendAckToken(List<String> msgUids, boolean isFinal) {
        StatusLine status = new BasicStatusLine(
                new ProtocolVersion("HTTP", 2, 0), 200, "");
        if (status.equals(tokensProxy.SendAckToken(msgUids, isFinal))) {
            registry.clearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    /**
     *
     * @param pinName
     * @param handle
     * @return
     */
    public short checkConnection(String pinName, @Nullable Map<String, String> handle) {
        try {
            DataHandle dHandle = GetDataHandle(pinName);
            return dHandle.checkConnection(handle);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot check connection for a pin of type \"Direct\"");
        }
    }

    private DataHandle GetDataHandle(String pinName) {
        if (null != dataHandles.get(pinName))
            return dataHandles.get(pinName);
        String accessType = registry.getPinConfiguration(pinName).accessType;
        DataHandle handle;
        switch (accessType) {
            case "Direct":
                throw new IllegalArgumentException(
                        "Cannot create a data handle for a pin of type \"Direct\"");
            case "MongoDB":
                handle = new MongoDbHandle(pinName, configuration);
                break;
            default:
                throw new UnsupportedOperationException("AccessType ("+ accessType +
                        ") not supported by the DataHandler, has to be handled manually");
        }
        dataHandles.put(pinName, handle);
        return handle;
    }
}
