package com.setcom.computation.dataaccess;

import com.setcom.computation.balticlsc.DataHandle;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.Dictionary;

public class MangoDbDataHandle extends DataHandle {

    private final String connectionString;
    private IMongoClient mongoClient;
    private IMongoDatabase mongoDatabase;
    private IMongoCollection<BsonDocument> mongoCollection;

    public MongoDbHandle(String pinName, JSONObject configuration) : base(pinName, configuration)
    {
        connectionString = $"mongodb://{PinConfiguration.AccessCredential["User"]}" +
            $":{PinConfiguration.AccessCredential["Password"]}" +
            $"@{PinConfiguration.AccessCredential["Host"]}" +
            $":{PinConfiguration.AccessCredential["Port"]}";
    }

    @Override
    public String Download(Dictionary<String, String> handle)
    {
        if ("input" != PinConfiguration.PinType)
            throw new Exception("Download cannot be called for output pins");
        if (!handle.TryGetValue("Database", out var databaseName))
            throw new ArgumentException("Incorrect DataHandle.");
        if (!handle.TryGetValue("Collection", out var collectionName))
            throw new ArgumentException("Incorrect DataHandle.");

        Prepare(databaseName, collectionName);

        var localPath = "";
        switch (PinConfiguration.DataMultiplicity)
        {
            case DataMultiplicity.Single:
            {
                if (!handle.TryGetValue("ObjectId", out String id))
                    throw new ArgumentException("Incorrect DataHandle.");
                try
                {
                    Log.Information($"Downloading object with id: {id}");
                    var filter = Builders<BsonDocument>.Filter.Eq("_id", new ObjectId(id));
                    var document = mongoCollection.Find(filter).FirstOrDefault();
                    if (document != null)
                    {
                        localPath = DownloadSingleFile(document, this.localPath);
                        Log.Information($"Downloading object with id: {id} successful.");
                    }
                    else
                    {
                        Log.Information($"Can not find object with id {id}");
                    }
                }
                catch (Exception)
                {
                    Log.Error($"Downloading object with id {id} failed.");
                    clearLocal();
                    throw;
                }

                break;
            }
            case DataMultiplicity.Multiple:
            {
                try
                {
                    Log.Information($"Downloading all files from {collectionName}.");
                    localPath = $"{LocalPath}/{collectionName}";
                    Directory.CreateDirectory(localPath);
                    var filter = Builders<BsonDocument>.Filter.Empty;
                    var documents = mongoCollection.Find(filter).ToList();

                    foreach (var document in documents)
                    DownloadSingleFile(document, localPath);

                    addGuidToFilesName(localPath);
                    Log.Information($"Downloading all files from {collectionName} successful.");
                }
                catch (Exception)
                {
                    Log.Error($"Downloading all files from collection {collectionName} failed.");
                    clearLocal();
                    throw;
                }

                break;
            }
        }

        return localPath;
    }

    @Override
    public Dictionary<String, String> upload(String localPath)
    {
        if ("input" == PinConfiguration.PinType)
            throw new Exception("Upload cannot be called for input pins");
        if (!File.Exists(localPath) && !Directory.Exists(localPath))
            throw new ArgumentException($"Invalid path ({localPath})");
        var isDirectory = File.GetAttributes(localPath).HasFlag(FileAttributes.Directory);
        if (DataMultiplicity.Multiple == PinConfiguration.DataMultiplicity && !isDirectory)
            throw new ArgumentException("Multiple data pin requires path pointing to a directory, not a file");
        if (DataMultiplicity.Single == PinConfiguration.DataMultiplicity && isDirectory)
            throw new ArgumentException("Single data pin requires path pointing to a file, not a directory");

        Dictionary<string, string> handle = null;
        try
        {
            var (databaseName, collectionName) = Prepare();

            switch (PinConfiguration.DataMultiplicity)
            {
                case DataMultiplicity.Single:
                {
                    Log.Information($"Uploading file from {localPath} to collection {collectionName}");

                    var bsonDocument = GetBsonDocument(localPath);
                    mongoCollection.InsertOne(bsonDocument);

                    handle = GetTokenHandle(bsonDocument);
                    handle.Add("Database", databaseName);
                    handle.Add("Collection", collectionName);

                    Log.Information($"Upload file from {localPath} successful.");
                    break;
                }
                case DataMultiplicity.Multiple:
                {
                    Log.Information($"Uploading directory from {localPath} to collection {collectionName}");
                    var files = GetAllFiles(localPath);
                    var handleList = new List<Dictionary<string, string>>();

                    foreach (var bsonDocument in files.Select(file => GetBsonDocument(file.FullName)))
                    {
                        mongoCollection.InsertOne(bsonDocument);
                        handleList.Add(GetTokenHandle(bsonDocument));
                    }

                    handle = new Dictionary<string, string>
                    {
                        {"Files", JsonConvert.SerializeObject(handleList)},
                        {"Database", databaseName},
                        {"Collection", collectionName}
                    };

                    Log.Information($"Upload directory from {localPath} successful.");
                    break;
                }
            }

            return handle;
        }
        catch (Exception e)
        {
            Log.Error($"Error: {e} \n Uploading from {localPath} failed.");
            throw;
        }
        finally
        {
            ClearLocal();
        }
    }

    public override short CheckConnection(Dictionary<String, String> handle = null)
    {
        String host = PinConfiguration.AccessCredential["Host"],
                port = PinConfiguration.AccessCredential["Port"];
        try
        {
            using var tcpClient = new TcpClient();
            tcpClient.Connect(host, int.Parse(port));
        }
        catch (Exception)
        {
            Log.Error($"Unable to reach {host}:{port}");
            return -1;
        }

        try
        {
            mongoClient = new MongoClient(connectionString);
            mongoClient.ListDatabases();
        }
        catch (MongoAuthenticationException)
        {
            Log.Error("Unable to authenticate to MongoDB");
            return -2;
        }
        catch (Exception e)
        {
            Log.Error($"Error {e} while trying to connect to MongoDB");
            return -1;
        }

        if ("input" == PinConfiguration.PinType && null != handle)
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

    private (String, String) Prepare(String databaseName = null, String collectionName = null)
    {
        databaseName ??= $"baltic_database_{Guid.NewGuid().ToString("N")[..8]}";
        collectionName ??= $"baltic_collection_{Guid.NewGuid().ToString("N")[..8]}";
        //TODO to reset or not to reset
        mongoClient = new MongoClient(connectionString);
        mongoDatabase = mongoClient.GetDatabase(databaseName);
        mongoCollection = mongoDatabase.GetCollection<BsonDocument>(collectionName);
        return (databaseName, collectionName);
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

    private static Dictionary<String, String> GetTokenHandle(BsonDocument document)
    {
        var newHandle = new Dictionary<String, String>()
        {
            {"FileName", document.GetElement("fileName").Value.AsString},
            {"ObjectId", document.GetElement("_id").Value.AsObjectId.ToString()}
        };

        return newHandle;
    }


}
