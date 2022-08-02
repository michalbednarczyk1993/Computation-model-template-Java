package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.InputTokenMessage;
import com.setcom.computation.datamodel.Status;
import org.javatuples.Pair;

import java.util.List;

public interface IJobRegistry {

    /**
     * Returns information about the tokens received for the given pin; uses the following status values:
     * Status.Idle (no tokens received),
     * Status.Working (at least one token received but more expected),
     * Status.Completed (all tokens received).
     *
     * @param pinName - that status is expected to be received
     * @return - status enum
     * @throws InterruptedException - if any thread interrupted the current thread before or while the current thread
     * was waiting. The interrupted status of the current thread is cleared when this exception is thrown.
     */
    Status getPinStatus(String pinName) throws InterruptedException;

    /**
     * Returns the “value” field of a token for a specified pin;
     * this is provided as a JSON-formatted string, containing either direct token data or reference information
     * for the file or folder in a remote storage.
     *
     * @param pinName JSON-formatted String, containing either direct token data or reference
     * information for the file or folder in a remote storage
     * @return - the “value” field of a token for a specified pin;
     * @throws Exception - thrown in case when more than one token exists for the pin
     */
    String getPinValue(String pinName) throws Exception;

    /**
     * Returns the “value” fields of a tokens for a specified pin;
     * this is provided as a JSON-formatted string, containing either direct token data or reference information
     * for the file or folder in a remote storage.
     *
     * @param pinName - JSON-formatted String, containing either direct token data or reference
     * information for the file or folder in a remote storage
     * @return - List of the “value” fields of a tokens for a specified pin;
     * @throws Exception - thrown in case when more than one token exists for the pin
     */
    List<String> getPinValues(String pinName) throws Exception;

    /**
     * GetPinValuesNDim(pinName: string): (List<string>, long[]) – as above but returns a
     * multidimensional matrix of “value” fields, stored as an array (list), together with the matrix
     * dimensions.
     *
     * @param pinName
     * @return
     * @throws InterruptedException
     */
    Pair<List<String>, long[]> getPinValuesNDim(String pinName) throws InterruptedException;

    /**
     * • GetPinTokens(pinName: string): List<InputTokenMessage> - returns a current list of received token
     * messages in the JSON format for the specified pin.
     *
     * @param pinName
     * @return -  current list of received token messages in the JSON format for the specified pin.
     */
    List<InputTokenMessage> getPinTokens(String pinName);

    /**
     * • SetProgress(progress: long): void – sets the value of the current progress; this value will be
     * reported to the BalticLSC system and displayed in the computation cockpit; the meaning of the
     * values depend on the module (completion percentage information
     *
     * @param progress
     */
    void setProgress(long progress);

    /**
     * • GetProgress(): long – returns the previously set progress value.
     *
     * @return
     */
    long getProgress();

    /**
     * SetStatus(status: Status): void – sets the current status of computations; this value will be reported
     * to the BalticLSC system and displayed in the computation cockpit; possible values are:
     *
     * Status.Idle (computations not yet started or is paused, e.g. waiting for further data),
     * Status.Working (computations in progress),
     * Status.Completed (computations are finished),
     * Status.Failed (computations have failed, e.g. the iteration limit has been reached without finding the result).
     *
     * @param status
     */
    void setStatus(Status status);

    /**
     * SetVariable(name: string, value: object): void – safely stores a value of a global variable; can be
     * used in case of multi-thread processing (e.g. communication between threads).
     *
     * @param name
     * @param value
     */
    void setVariable(String name, String value);

    /**
     * GetVariable(name: string): object – retrieves a global variable value (see above).
     *
     * @param name
     * @return
     */
    Object getVariable(String name);

    /**
     * GetEnvironmentVariable(name: string): string – retrieves a system variable supplied by the
     * external execution environment; can be used to parameterise the module’s behaviour;
     * such userdefined variables can be set when defining computation modules in the BalticLSC web interface.
     *
     * @param name
     * @return
     */
    String getEnvironmentVariable(String name);
}
