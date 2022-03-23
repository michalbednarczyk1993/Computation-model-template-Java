package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.Dictionary;

@Slf4j
public abstract class DataHandle {

    protected final PinConfiguration pinConfiguration;
    protected final String LocalPath;

    private static final String BALTIC_DATA_PATH = "/BalticLSC/data";
    private static final String BALTIC_DATA_PREFIX = "BalticLSC-";
    private static final int GUID_LENGTH = 6;

    ///
    /// <param name="pinName"></param>
    /// <param name="configuration"></param>
    protected DataHandle(String pinName, IConfiguration configuration)
    {
        LocalPath = Environment.GetEnvironmentVariable("LOCAL_TMP_PATH") ?? "/balticLSC_tmp";

        try
        {
            PinConfiguration =
                    ConfigurationHandle.GetPinsConfiguration(configuration).Find(x => x.PinName == pinName);

        }
        catch (Exception)
        {
            Log.Error("Error while parsing configuration.");
        }

        Directory.CreateDirectory(LocalPath);

    }

    public abstract short CheckConnection(@Nullable Dictionary<String, String> handle);

    ///
    /// <param name="handle"></param>
    public abstract String Download(Dictionary<String, String> handle);

    ///
    /// <param name="localPath"></param>
    public abstract Dictionary<String, String> Upload(String localPath);

    protected void ClearLocal()
    {
        try
        {
            if (Directory.Exists(LocalPath))
            {
                Directory.Delete(LocalPath, true);
            }
            else if (File.Exists(LocalPath))
            {
                File.Delete(LocalPath);
            }
        }
        catch (Exception e)
        {
            log.error("Error while clearing local memory: " + e;
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
