package com.setcom.computation.dataaccess;

import com.mongodb.lang.Nullable;
import com.setcom.computation.balticlsc.DataHandle;
import com.setcom.computation.datamodel.DataMultiplicity;
import com.setcom.computation.datamodel.PinConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Directory;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.javatuples.Pair;
import org.springframework.asm.Handle;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;


@Slf4j
public class MangoDbDataHandle extends DataHandle {

    private final String connectionString;
    private IMongoClient mongoClient;
    private IMongoDatabase mongoDatabase;
    private IMongoCollection<BsonDocument> mongoCollection;

    public MongoDbHandle(String pinName, JSONObject configuration) {
        super(pinName, configuration);
        connectionString = "mongodb://{PinConfiguration.AccessCredential[\"" + User + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + Password + "\"]}" +
            "@{PinConfiguration.AccessCredential[\"" + Host + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + Port + "\"]}";
    }

    @Override
    public String Download(HashMap<String, String> handle) throws Exception {
        String databaseName = handle.getOrDefault("Database", "");
        String collectionName = handle.getOrDefault("Collection", "");
        if (!pinConfiguration.pinType.equals("input"))
            throw new Exception("Download cannot be called for output pins");
        if (databaseName.isEmpty())
            throw new IllegalArgumentException("Incorrect DataHandle.");
        if (collectionName.isEmpty())
            throw new IllegalArgumentException("Incorrect DataHandle.");

        Prepare(databaseName, collectionName);

        String localPath = "";
        String id = handle.getOrDefault("ObjectId", "");
        switch (pinConfiguration.dataMultiplicity) {
            case DataMultiplicity.SINGLE:
            {
                if (id.isEmpty())
                    throw new IllegalArgumentException("Incorrect DataHandle.");
                try {
                    log.info("Downloading object with id: " + id);
                    var filter = Builders<BsonDocument>.Filter.Eq("_id", new ObjectId(id));
                    var document = mongoCollection.Find(filter).FirstOrDefault();
                    if (document != null) {
                        localPath = DownloadSingleFile(document, this.localPath);
                        log.info("Downloading object with id: " + id + "successful.");
                    } else {
                        log.info("Can not find object with id: " + id);
                    }
                }
                catch (Exception e) {
                    log.error("Downloading object with id: " + id + "failed.");
                    clearLocal();
                    throw e;
                }

                break;
            }
            case DataMultiplicity.MULTIPLE:
            {
                try {
                    log.info("Downloading all files from " + collectionName);
                    localPath = localPath + "/" + collectionName;
                    Directory.CreateDirectory(localPath);
                    var filter = Builders<BsonDocument>.Filter.Empty;
                    var documents = mongoCollection.Find(filter).ToList();

                    for (var document : documents)
                        DownloadSingleFile(document, localPath);

                    addGuidToFilesName(localPath);
                    log.info("Downloading all files from " + collectionName + " successful.");
                }
                catch (Exception e) {
                    log.error("Downloading all files from collection " + collectionName + " failed.");
                    clearLocal();
                    throw e;
                }

                break;
            }
        }

        return localPath;
    }

    @Override
    public HashMap<String, String> upload(String localPath) throws Exception {
        if (pinConfiguration.pinType.equals("input"))
            throw new Exception("Upload cannot be called for input pins");
        if (!new File(localPath).exists())
            throw new IllegalArgumentException("Invalid path (" + localPath + ")");
        boolean isDirectory = File.GetAttributes(localPath).HasFlag(FileAttributes.Directory);
        if (pinConfiguration.dataMultiplicity.equals(DataMultiplicity.MULTIPLE) && !isDirectory)
            throw new IllegalArgumentException("Multiple data pin requires path pointing to a directory, not a file");
        if (pinConfiguration.dataMultiplicity.equals(DataMultiplicity.SINGLE) && isDirectory)
            throw new IllegalArgumentException("Single data pin requires path pointing to a file, not a directory");

        HashMap<String, String> handle = null;
        try {
            Pair<String, String> pair = Prepare();
            String databaseName = pair.getValue0(), collectionName = pair.getValue1();

            switch (pinConfiguration.dataMultiplicity) {
                case DataMultiplicity.SINGLE:
                {
                    log.info("Uploading file from " + localPath + " to collection " + collectionName);

                    var bsonDocument = GetBsonDocument(localPath);
                    mongoCollection.InsertOne(bsonDocument);

                    handle = getTokenHandle(bsonDocument);
                    handle.put("Database", databaseName);
                    handle.put("Collection", collectionName);

                    log.info("Upload file from " + localPath + " successful.");
                    break;
                }
                case DataMultiplicity.MULTIPLE:
                {
                    log.info("Uploading directory from " + localPath + " to collection " + collectionName);
                    var files = getAllFiles(localPath);
                    var handleList = new ArrayList<HashMap<String, String>>();

                    for (var bsonDocument : files.stream().filter(file-> GetBsonDocument(file.fullName))) {
                        mongoCollection.InsertOne(bsonDocument);
                        handleList.add(getTokenHandle(bsonDocument));
                    }

                    handle = new HashMap<>();
                    handle.put("Files", JsonConvert.SerializeObject(handleList));
                    handle.put("Database", databaseName);
                    handle.put("Collection", collectionName);

                    log.info("Upload directory from " + localPath + " successful.");
                    break;
                }
            }

            return handle;
        }
        catch (Exception e) {
            log.error("Error: " + e + "\n Uploading from " + localPath +  " failed.");
            throw e;
        } finally {
            clearLocal();
        }
    }

    public short checkConnection(@Nullable HashMap<String, String> handle) {
        String host = pinConfiguration.accessCredential.get("Host"),
                port = pinConfiguration.accessCredential.get("Port");
        try {
            using var tcpClient = new TcpClient();
            tcpClient.Connect(host, int.Parse(port));
        } catch (Exception e) {
            log.error("Unable to reach " + host + ":" + port);
            return -1;
        }

        try {
            mongoClient = new MongoClient(connectionString);
            mongoClient.ListDatabases();
        } catch (MongoAuthenticationException e) {
            log.error("Unable to authenticate to MongoDB");
            return -2;
        } catch (Exception e) {
            log.error("Error " + e + " while trying to connect to MongoDB");
            return -1;
        }

        if (pinConfiguration.pinType.equals("input") && null != handle)
        {
            if (!handle.TryGetValue("Database", out var databaseName))
                throw new ArgumentException("Incorrect DataHandle.");
            if (!handle.TryGetValue("Collection", out var collectionName))
                throw new ArgumentException("Incorrect DataHandle.");
            String id = null;
            if (PinConfiguration.DataMultiplicity == DataMultiplicity.Single
                    && !handle.TryGetValue("ObjectId", out id))
                throw new ArgumentException("Incorrect DataHandle.");
            try
            {
                mongoDatabase = mongoClient.GetDatabase(databaseName);
                if (mongoDatabase == null)
                {
                    Log.Error($"No database {databaseName}");
                    return -3;
                }

                mongoCollection = mongoDatabase.GetCollection<BsonDocument>(collectionName);
                if (mongoCollection == null)
                {
                    Log.Error($"No collection {collectionName}");
                    return -3;
                }

                if (PinConfiguration.DataMultiplicity == DataMultiplicity.Single)
                {
                    var filter = Builders<BsonDocument>.Filter.Eq("_id", new ObjectId(id));
                    var document = mongoCollection.Find(filter).FirstOrDefault();

                    if (document == null)
                    {
                        Log.Error($"No document with id {id}");
                        return -3;
                    }
                }
            }
            catch (Exception)
            {
                Log.Error("Error while trying to " +
                        (null != id ? $"get object {id}" : $"access collection {collectionName}") +
                    $" from database {databaseName}" +
                    (null != id ? $" from collection {collectionName}" : ""));
                return -3;
            }
        }

        return 0;
    }

    private Pair<String, String> Prepare(String databaseName = null, String collectionName = null) {
        databaseName ??= $"baltic_database_{Guid.NewGuid().ToString("N")[..8]}";
        collectionName ??= $"baltic_collection_{Guid.NewGuid().ToString("N")[..8]}";
        //TODO to reset or not to reset
        mongoClient = new MongoClient(connectionString);
        mongoDatabase = mongoClient.GetDatabase(databaseName);
        mongoCollection = mongoDatabase.GetCollection<BsonDocument>(collectionName);
        return new Pair<>(databaseName, collectionName);
    }

    private static String DownloadSingleFile(BsonDocument document, String localPath)
    {
        var fileName = document.GetElement("fileName").Value.AsString;
        var fileContent = document.GetElement("fileContent").Value.AsBsonBinaryData;
        var filePath = $"{localPath}/{fileName}";
        using var fileStream = File.OpenWrite(filePath);
        fileStream.Write(fileContent.Bytes);
        fileStream.Dispose();

        return filePath;
    }

    private static BsonDocument GetBsonDocument(String localPath)
    {
        var objectId = ObjectId.GenerateNewId();
        var fileStream = File.OpenRead(localPath);
        var fileName = new FileInfo(localPath).Name;
        var memoryStream = new MemoryStream();
        fileStream.CopyTo(memoryStream);
        var fileByteArray = memoryStream.ToArray();

        var data = new Dictionary<String, object>()
        {
            {"_id", objectId},
            {"fileName", fileName},
            {"fileContent", new BsonBinaryData(fileByteArray)}
        };

        var bsonDocument = new BsonDocument(data);

        return bsonDocument;
    }

    private static HashMap<String, String> getTokenHandle(BsonDocument document) {
        return new HashMap<String, String>() {
            {"FileName", document.GetElement("fileName").Value.AsString},
            {"ObjectId", document.GetElement("_id").Value.AsObjectId.ToString()}
        };
    }
}
