package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.lang.Nullable;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.HashMap;

@Slf4j
public abstract class DataHandle {

    protected PinConfiguration pinConfiguration = null;
    protected final String localPath;

    private static final String BALTIC_DATA_PATH = "/BalticLSC/data";
    private static final String BALTIC_DATA_PREFIX = "BalticLSC-";
    private static final int GUID_LENGTH = 6;

    ///
    /// <param name="pinName"></param>
    /// <param name="configuration"></param>
    protected DataHandle(String pinName, JSONObject configuration)
    {
        localPath = System.getenv("LOCAL_TMP_PATH") != null ?
                System.getenv("LOCAL_TMP_PATH") : "/balticLSC_tmp";

        try {
            pinConfiguration = ConfigurationHandle.GetPinsConfiguration(configuration).
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

    public abstract short checkConnection(@Nullable HashMap<String, String> handle);

    ///
    /// <param name="handle"></param>
    public abstract String Download(Dictionary<String, String> handle);

    ///
    /// <param name="localPath"></param>
    public abstract Dictionary<String, String> Upload(String localPath);

    protected void ClearLocal()
    {
        try {
            File directory = new File(localPath);
            if (directory.exists()) {
                FileSystemUtils.deleteRecursively(Paths.get(localPath));
            }
        } catch (IOException | SecurityException e) {
            log.error("Error while clearing local memory:  " + e.toString());
        }
    }

    protected void AddGuidToFilesName(String directoryPath)
    {
        var files = new DirectoryInfo(directoryPath).GetFiles();
        for (var file : files)
        {
            var filePath = file.FullName;
            var fileName = Path.GetFileName(filePath);
            var newFileName = GetNameWithGuid(fileName);
            File.Move(filePath,Path.Combine(directoryPath, newFileName));
        }
    }

    private String GetNameWithGuid(String name)
    {
        if(name.StartsWith(BalticDataPrefix))
        {
            return name.Remove(BalticDataPrefix.Length,GuidLength).Insert(BalticDataPrefix.Length,Guid.NewGuid().ToString().Substring(0, GuidLength));
        }
        return BalticDataPrefix + Guid.NewGuid().ToString().Substring(0, GuidLength)+"-" + name;
    }

    protected List<FileInfo> GetAllFiles(String directoryPath)
    {
        if (!Directory.Exists(directoryPath))
        {
            return new List<FileInfo>();
        }

        var directoryInfo = new DirectoryInfo(directoryPath);
        var files = directoryInfo.GetFiles().ToList();
        var directories = directoryInfo.GetDirectories().ToList();

        directories.ForEach(x => files.AddRange(GetAllFiles(x.FullName)));

        return files;
    }
}
