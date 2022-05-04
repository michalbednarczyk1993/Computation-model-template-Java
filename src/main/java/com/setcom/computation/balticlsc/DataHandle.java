package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.lang.Nullable;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public abstract class DataHandle {

    protected PinConfiguration pinConfiguration = null;
    protected final String localPath;

    private static final String BALTIC_DATA_PATH = "/BalticLSC/data";
    private static final String BALTIC_DATA_PREFIX = "BalticLSC-";
    private static final int GUID_LENGTH = 6;


    /**
     *
     * @param pinName
     * @param configuration
     */
    protected DataHandle(String pinName, JSONObject configuration)
    {
        localPath = System.getenv("LOCAL_TMP_PATH") != null ?
                System.getenv("LOCAL_TMP_PATH") : "/balticLSC_tmp";

        try {
            pinConfiguration = Objects.requireNonNull(ConfigurationHandle.GetPinsConfiguration(configuration)).
                    stream().filter((x)-> x.pinName.equals(pinName)).findAny().orElse(null);
        } catch (JSONException e) {
            log.error("Error while parsing configuration.");
            log.error(e.toString());
        }

        File dir = new File(localPath);
        if (!dir.exists()) {
            log.info("DataHandle create directory with localPath: " + dir.mkdir());
        }
    }

    /**
     *
     * @param handle
     * @return
     */
    public abstract short checkConnection(@Nullable Map<String, String> handle);

    /**
     *
     * @param handle
     * @return
     */
    public abstract String Download(HashMap<String, String> handle) throws Exception;

    /**
     *
     * @param localPath
     * @return
     */
    public abstract HashMap<String, String> upload(String localPath) throws Exception;

    protected void clearLocal()
    {
        try {
            File directory = new File(localPath);
            if (directory.exists()) {
                FileSystemUtils.deleteRecursively(Paths.get(localPath));
            }
        } catch (IOException | SecurityException e) {
            log.error("Error while clearing local memory:  " + e);
        }
    }

    protected void addGuidToFilesName(String directoryPath) {
        var files = new File(directoryPath).listFiles();
        if (files == null) {
            log.error("Provided directory is invalid or does not contain any files and subdirectories");
            return;
        }

        for (var file : files) {
            Path fullPath = Paths.get(file.getPath());
            var fileName = file.getName();
            var newName = getNameWithGuid(fileName);
            boolean result = file.renameTo(new File(fullPath.resolve(newName).toString()));
            if (!result)
                log.error("Changing file directory failed");
        }
    }

    private String getNameWithGuid(String name)
    {
        if(name.startsWith(BALTIC_DATA_PREFIX))
        {
            return new StringBuilder(name).delete(BALTIC_DATA_PREFIX.length(), BALTIC_DATA_PREFIX.length() + GUID_LENGTH).
                    insert(BALTIC_DATA_PREFIX.length(), UUID.randomUUID().toString().substring(0, GUID_LENGTH)).toString();
        }
        return BALTIC_DATA_PREFIX + UUID.randomUUID().toString().substring(0, GUID_LENGTH) + "-" + name;
    }

    protected List<String> getAllFiles(String directoryPath) {
        List<String> result = new ArrayList<>();
        try {
            File rootDir = new File(localPath);
            if (!rootDir.exists()) {
                log.warn("Provided directory does not exist.");
                return result;
            }

            File[] list = Objects.requireNonNull(rootDir.listFiles());

            for (File file : list) {
                if (file.isFile()) {
                    result.add(file.getAbsolutePath());
                } else {
                    result.addAll(getAllFiles(file.getAbsolutePath()));
                }
            }

        } catch (NullPointerException | SecurityException e) {
            log.error(e.toString());
            return result;
        }

        return result;
    }

}
