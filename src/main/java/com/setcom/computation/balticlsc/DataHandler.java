package com.setcom.computation.balticlsc;

import com.google.gson.Gson;
import com.setcom.computation.apiaccess.TokensProxy;
import com.setcom.computation.datamodel.Status;
import org.javatuples.Pair;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.*;

public class DataHandler implements IDataHandler{

    private final HashMap<String, DataHandle> dataHandles;
    private final TokensProxy tokensProxy;
    private final JobRegistry registry;
    private final JSONObject configuration;

    /**
     *
     * @param registry
     * @param configuration
     */
    public DataHandler(JobRegistry registry, JSONObject configuration)
    {
        this.registry = registry;
        this.tokensProxy = new TokensProxy();
        this.configuration = configuration;
        this.dataHandles = new HashMap<>();
    }

    @Override
    public String ObtainDataItem(String pinName) throws Exception {
        Pair<List<String>, long[]> obtainData = ObtainDataItemsNDim(pinName);
        if (null == obtainData.getValue0() || 0 == obtainData.getValue0().size())
            return null;
        if (null == obtainData.getValue1() && 1 == obtainData.getValue0().size())
            return obtainData.getValue0().get(0);
        throw new Exception("Improper call - more than one data item exists for the pin");
    }

    @Override
    public List<String> ObtainDataItems(String pinName) throws Exception {
        Pair<List<String>, long[]> obtainData = ObtainDataItemsNDim(pinName);
        if (obtainData.getValue1() != null && obtainData.getValue1().length == 1) {
            return obtainData.getValue0();
        }
        throw new Exception("Improper call - more than one dimension exists for the pin");
    }

    /**
     *
     * @param pinName
     * @return
     */
    @Override
    public Pair<List<String>, long[]> ObtainDataItemsNDim(String pinName) {
        try {
            Pair<List<String>, long[]> pair = registry.GetPinValuesNDim(pinName);
            List<HashMap<String,String>> valuesObject = new ArrayList<>();
            for (String value : pair.getValue0()) {
                if (value != null && !value.isEmpty()) {
                    HashMap<String, String> map = new Gson().fromJson(value, HashMap.class);
                    valuesObject.add(map);
                }
            }

            DataHandle dHandle = GetDataHandle(pinName);
            List<String> dataItems = new ArrayList<>();
            for (HashMap<String, String> value : valuesObject) {
                if (value != null) {
                    dataItems.add(dHandle.Download(value));
                }
            }

            return new Pair<>(dataItems, pair.getValue1());
        }catch (IllegalArgumentException e) {
            return registry.GetPinValuesNDim(pinName);
        }
    }

    @Override
    public short SendDataItem(String pinName, String data, boolean isFinal, @Nullable String msgUid) {
        if ("Direct".equals(registry.GetPinConfiguration(pinName).accessType))
            return SendToken(pinName, data, isFinal, msgUid);
        DataHandle dHandle = GetDataHandle(pinName);
        Map<String,String> newHandle = dHandle.upload(data);

        return SendToken(pinName, new Gson().toJson(newHandle), isFinal, msgUid);
    }

    @Override
    public short SendDataItem(String pinName, String data, boolean isFinal) {
        return SendDataItem(pinName, data, isFinal, null);
    }

    @Override
    public short SendToken(String pinName, String values, boolean isFinal, @Nullable String msgUid) {
        if (null == msgUid)
            msgUid = registry.GetBaseMsgUid();
        return HttpStatus.OK.value() == tokensProxy.SendOutputToken(pinName, values, msgUid, isFinal).getStatusCode()
                ? (short)0 : (short)-1;
    }

    @Override
    public short SendToken(String pinName, String values, boolean isFinal) {
        return SendToken(pinName, values, isFinal, null);
    }

    public short FinishProcessing() {
        List<String> msgUids = registry.GetAllMsgUids();
        registry.SetStatus(Status.COMPLETED);
        return SendAckToken(msgUids, true);
    }

    @Override
    public short FailProcessing(String note) {
        List<String> msgUids = registry.GetAllMsgUids();
        registry.SetStatus(Status.FAILED);
        if (HttpStatus.OK.value() == tokensProxy.SendAckToken(msgUids, true, true, note).getStatusCode()) {
            registry.ClearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    @Override
    public short SendAckToken(List<String> msgUids, boolean isFinal) {
        if (HttpStatus.OK.value() == tokensProxy.SendAckToken(msgUids, isFinal).getStatusCode()) {
            registry.ClearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    public short CheckConnection(String pinName, @Nullable Map<String, String> handle) {
        try {
            DataHandle dHandle = GetDataHandle(pinName);
            return dHandle.checkConnection(handle);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot check connection for a pin of type \"Direct\"");
        }
    }

    private DataHandle GetDataHandle(String pinName)
    {
        if (null != dataHandles.get(pinName))
            return dataHandles.get(pinName);
        String accessType = registry.GetPinConfiguration(pinName).accessType;
        DataHandle handle;
        switch (accessType)
        {
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
