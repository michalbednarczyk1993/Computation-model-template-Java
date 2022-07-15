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
    String obtainDataItem(String pinName) throws Exception;

    /**
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    List<String> obtainDataItems(String pinName) throws Exception;

    /**
     *
     * @param pinName
     * @return
     */
    Pair<List<String>, long[]> obtainDataItemsNDim(String pinName);

    /**
     *
     * @param pinName
     * @param data
     * @param isFinal
     * @param msgUid
     * @return
     */
    short sendDataItem(String pinName, String data, boolean isFinal, @Nullable String msgUid) throws Exception;

    /**
     *
     * @param pinName
     * @param values
     * @param isFinal
     * @param msgUid
     * @return
     */
    short sendToken(String pinName, String values, boolean isFinal, @Nullable String msgUid);

    short finishProcessing();

    /**
     *
     * @param msgUids
     * @param isFinal
     * @return
     */
    short sendAckToken(List<String> msgUids, boolean isFinal);
}
