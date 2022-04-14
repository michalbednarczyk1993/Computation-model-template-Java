package com.setcom.computation.balticlsc;

import org.javatuples.Pair;

import java.util.List;

public interface IDataHandler {
    ///
    /// <param name="pinName"></param>
    String ObtainDataItem(String pinName) throws Exception;

    ///
    /// <param name="pinName"></param>
    List<String> ObtainDataItems(String pinName) throws Exception;

    ///
    /// <param name="pinName"></param>
    Pair<List<String>, long[]> ObtainDataItemsNDim(String pinName);

    ///
    /// <param name="pinName"></param>
    /// <param name="data"></param>
    /// <param name="isFinal"></param>
    /// <param name="msgUid"></param>
    short SendDataItem(String pinName, String data, boolean isFinal, String msgUid=null);

    ///
    /// <param name="pinName"></param>
    /// <param name="values"></param>
    /// <param name="isFinal"></param>
    /// <param name="msgUid"></param>
    short SendToken(String pinName, String values, boolean isFinal, String msgUid = null);

    short FinishProcessing();

    ///
    /// <param name="msgUids"></param>
    /// <param name="isFinal"></param>
    short SendAckToken(List<String> msgUids, boolean isFinal);
}
