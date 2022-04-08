package com.setcom.computation.balticlsc;

import org.javatuples.Pair;

import java.util.List;

public interface IDataHandler {

    String ObtainDataItem(String pinName) throws Exception;

    List<String> ObtainDataItems(String pinName) throws Exception;

    Pair<List<String>, long[]> ObtainDataItemsNDim(String pinName);

    short SendDataItem(String pinName, String data, boolean isFinal, String msgUid);

    short SendDataItem(String pinName, String data, boolean isFinal);

    short SendToken(String pinName, String values, boolean isFinal, String msgUid);

    short SendToken(String pinName, String values, boolean isFinal);

    short FinishProcessing();

    short SendAckToken(List<String> msgUids, boolean isFinal);
}
