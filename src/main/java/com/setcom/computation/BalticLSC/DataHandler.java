package com.setcom.computation.BalticLSC;

import org.javatuples.Pair;

import java.util.Dictionary;
import java.util.List;

public class DataHandler implements IDataHandler{
    private Dictionary<String, DataHandle> dataHandles;
    private final TokensProxy tokensProxy;
    private final JobRegistry registry;
    private final IConfiguration configuration;

    ///
    /// <param name="registry"></param>
    /// <param name="configuration"></param>
    public DataHandler(JobRegistry registry, IConfiguration configuration)
    {
        this.registry = registry;
        this.tokensProxy = new TokensProxy();
        this.configuration = configuration;
        this.dataHandles = new Dictionary<string, DataHandle>();
    }

    public String ObtainDataItem(String pinName)
    {
        List<String> values;
        long[] sizes;
        (values, sizes) = ObtainDataItemsNDim(pinName); //TODO how to handle it?
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
        Dictionary<string,string> newHandle = dHandle.Upload(data);
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

    public short FailProcessing(String note)
    {
        List<string> msgUids = _registry.GetAllMsgUids();
        _registry.SetStatus(Status.Failed);
        if (HttpStatusCode.OK == _tokensProxy.SendAckToken(msgUids, true, true, note))
        {
            _registry.ClearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    ///
    /// <param name="msgUids"></param>
    /// <param name="isFinal"></param>
    public short SendAckToken(List<string> msgUids, bool isFinal)
    {
        if (HttpStatusCode.OK == _tokensProxy.SendAckToken(msgUids, isFinal))
        {
            _registry.ClearMessages(msgUids);
            return 0;
        }
        return -1;
    }

    ///
    /// <param name="pinName"></param>
    /// <param name="handle"></param>
    public short CheckConnection(string pinName, Dictionary<string,string> handle = null)
    {
        try
        {
            DataHandle dHandle = GetDataHandle(pinName);
            return dHandle.CheckConnection(handle);
        }
        catch (ArgumentException)
        {
            throw new ArgumentException(
                    "Cannot check connection for a pin of type \"Direct\"");
        }
    }

    private DataHandle GetDataHandle(string pinName)
    {
        if (_dataHandles.ContainsKey(pinName))
            return _dataHandles[pinName];
        string accessType = _registry.GetPinConfiguration(pinName).AccessType;
        DataHandle handle;
        switch (accessType)
        {
            case "Direct":
                throw new ArgumentException(
                        "Cannot create a data handle for a pin of type \"Direct\"");
            case "MongoDB":
                handle = new MongoDbHandle(pinName, _configuration);
                break;
            default:
                throw new NotImplementedException(
                        $"AccessType ({accessType}) not supported by the DataHandler, has to be handled manually");
        }
        _dataHandles[pinName] = handle;
        return handle;
    }
}
