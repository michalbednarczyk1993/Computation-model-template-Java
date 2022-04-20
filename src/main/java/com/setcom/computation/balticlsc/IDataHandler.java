package com.setcom.computation.balticlsc;

import org.javatuples.Pair;
import org.springframework.lang.Nullable;

import java.util.List;

public interface IDataHandler {

    /**
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    String ObtainDataItem(String pinName) throws Exception;

    /**
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    List<String> ObtainDataItems(String pinName) throws Exception;

    /**
     *
     * @param pinName
     * @return
     */
    Pair<List<String>, long[]> ObtainDataItemsNDim(String pinName);

    /**
     *
     * @param pinName
     * @param data
     * @param isFinal
     * @param msgUid
     * @return
     */
    short SendDataItem(String pinName, String data, boolean isFinal, @Nullable String msgUid);

    /**
     *
     * @param pinName
     * @param values
     * @param isFinal
     * @param msgUid
     * @return
     */
    short SendToken(String pinName, String values, boolean isFinal, @Nullable String msgUid);

    short FinishProcessing();

    /**
     *
     * @param msgUids
     * @param isFinal
     * @return
     */
    short SendAckToken(List<String> msgUids, boolean isFinal);
}
