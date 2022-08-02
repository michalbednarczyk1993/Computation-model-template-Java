package com.setcom.computation.balticlsc;

import org.javatuples.Pair;
import org.springframework.lang.Nullable;

import java.util.List;

public interface IDataHandler {

    /**
     * ObtainDataItem(pinName: string): string – downloads a data item (file or folder) from a remote
     * storage and returns the full path to it in the local file system, for the specified pin.
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    String obtainDataItem(String pinName) throws Exception;

    /**
     * ObtainDataItems(pinName: string): List<string> - as above but returns several path strings for pins
     * with multiple tokens.
     *
     * @param pinName
     * @return
     * @throws Exception
     */
    List<String> obtainDataItems(String pinName) throws Exception;

    /**
     * ObtainDataItemsNDim(pinName: string): (List<string>, long[]) - as above but returns a
     * multidimensional matrix of path strings, stored as an array (list), together with the matrix
     * dimensions.
     *
     * @param pinName
     * @return
     */
    Pair<List<String>, long[]> obtainDataItemsNDim(String pinName);

    /**
     *SendDataItem(pinName: string, data: string, isFinal: bool, msgUid: string = null): short – uploads a
     * data item (file or folder) to a remote storage for the specified pin; the isFinal parameter is used
     * to mark the last element in the sequence (for multiple token pins); the optional msgUid
     * parameter allows to specify a trace to the input data item for which the current data item (token)
     * is produced.
     *
     * @param pinName
     * @param data
     * @param isFinal
     * @param msgUid
     * @return
     */
    short sendDataItem(String pinName, String data, boolean isFinal, @Nullable String msgUid) throws Exception;

    /**
     * SendToken(pinName: string, values: string, isFinal: bool, msgUid: string = null): short – as above but
     * used for direct data (no need to upload data); the data should be provided through the “values”
     * parameter in the JSON format (see GetPinValue in IJobRegistry).
     *
     * @param pinName
     * @param values
     * @param isFinal
     * @param msgUid
     * @return
     */
    short sendToken(String pinName, String values, boolean isFinal, @Nullable String msgUid);

    /**
     * FinishProcessing(): short – cleans-up the environment for the given job and sends the status to
     * Status.Completed; should be called only after completing all the computations.
     *
     * @return
     */
    short finishProcessing();

    /**
     * SendAckToken(msgUids: List<string>, isFinal: bool): short - cleans-up after finishing processing of
     * individual data items (tokens); sends a token notifying the execution environment about that
     * some input data item (token) has been fully processed; this can be used to optimize distributed
     * processing (can start further jobs earlier); setting the “isFinal” parameter to “true” indicates that
     * we do not expect any jobs related to the given data item sequence to be run in the overall
     * computation task – allows to optimize distributed processing.
     *
     * @param msgUids
     * @param isFinal
     * @return
     */
    short sendAckToken(List<String> msgUids, boolean isFinal);
}
