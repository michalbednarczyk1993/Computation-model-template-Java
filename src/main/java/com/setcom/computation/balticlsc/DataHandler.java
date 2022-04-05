package com.setcom.computation.balticlsc;

import com.setcom.computation.apiaccess.TokensProxy;
import com.setcom.computation.datamodel.Status;
import org.javatuples.Pair;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandler implements IDataHandler{

    private final HashMap<String, DataHandle> dataHandles;
    private final TokensProxy tokensProxy;
    private final JobRegistry registry;
    private final JSONObject configuration;

    ///
    /// <param name="registry"></param>
    /// <param name="configuration"></param>
    public DataHandler(JobRegistry registry, JSONObject configuration)
    {
        this.registry = registry;
        this.tokensProxy = new TokensProxy();
        this.configuration = configuration;
        this.dataHandles = new HashMap<>();
    }

    public String ObtainDataItem(String pinName) {
        List<String> values;
        long[] sizes;
        //(values, sizes) = ObtainDataItemsNDim(pinName); //TODO how to handle it?
        Pair<List<String>, long[]> obtainData = ObtainDataItemsNDim(pinName);
        obtainData.
        if (null == values || 0 == values.size())
            return null;
        if (null == sizes && 1 == values.size())
            return values.get(0);
        throw new Exception("Improper call - more than one data item exists for the pin");
    }

    public List<String> ObtainDataItems(String pinName)
    {
        List<String> values;
        long[] sizes;
        (values, sizes) = ObtainDataItemsNDim(pinName);
        if (1 == sizes?.Length) //TODO how to convert it?
        return values;
        throw new Exception("Improper call - more than one dimension exists for the pin");
    }

    ///
    /// <param name="pinName"></param>
    public Pair<List<String>, long[]> ObtainDataItemsNDim(String pinName)
    {
        try
        {
            List<String> values;
            long[] sizes;
            (values, sizes) = registry.GetPinValuesNDim(pinName);
            List<Dictionary<String,String>> valuesObject =
            values.Select(v => !string.IsNullOrEmpty(v) ? JsonConvert.DeserializeObject<Dictionary<string, string>>(v) : null).ToList();
            DataHandle dHandle = GetDataHandle(pinName);
            List<string> dataItems = valuesObject.Select(vo => null != vo ? dHandle.Download(vo): null).ToList();
            return (dataItems, sizes);
        }
        catch (ArgumentException)
        {
            return _registry.GetPinValuesNDim(pinName);
        }
    }

    ///
    /// <param name="pinName"></param>
    /// <param name="data"></param>
    /// <param name="isFinal"></param>
    /// <param name="msgUid"></param>
    public short SendDataItem(string pinName, string data, bool isFinal, string msgUid = null)
    {
        if ("Direct" == _registry.GetPinConfiguration(pinName).AccessType)
            return SendToken(pinName, data, isFinal, msgUid);
        DataHandle dHandle = GetDataHandle(pinName);
        Dictionary<string,string> newHandle = dHandle.upload(data);
        return SendToken(pinName, JsonConvert.SerializeObject(newHandle), isFinal, msgUid);
    }

    ///
    /// <param name="pinName"></param>
    /// <param name="values"></param>
    /// <param name="isFinal"></param>
    /// <param name="msgUid"></param>
    public short SendToken(string pinName, string values, bool isFinal, string msgUid = null)
    {
        if (null == msgUid)
            msgUid = _registry.GetBaseMsgUid();
        return HttpStatusCode.OK == _tokensProxy.SendOutputToken(pinName, values, msgUid, isFinal) ? (short)0 : (short)-1;
    }

    public short FinishProcessing()
    {
        List<string> msgUids = _registry.GetAllMsgUids();
        _registry.SetStatus(Status.Completed);
        return SendAckToken(msgUids, true);
    }

    public short FailProcessing(String note) {
        List<String> msgUids = registry.GetAllMsgUids();
        registry.SetStatus(Status.FAILED);
        if (HttpStatus.OK.value() == tokensProxy.SendAckToken(msgUids, true, true, note).getStatusCode()) {
            registry.ClearMessages(msgUids);
            return 0;
        }
        return -1;
    }

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
