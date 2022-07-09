package com.setcom.computation.dataaccess;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.lang.Nullable;
import com.setcom.computation.balticlsc.DataHandle;
import com.setcom.computation.datamodel.DataMultiplicity;
import lombok.extern.slf4j.Slf4j;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.javatuples.Pair;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.mongodb.client.model.Filters.*;


@Slf4j
public class MangoDbHandle extends DataHandle {

    private final String connectionString;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<BsonDocument> mongoCollection;

    public MangoDbHandle(String pinName, JSONObject configuration) {
        super(pinName, configuration);
        connectionString = "mongodb://{PinConfiguration.AccessCredential[\"" + user + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + password + "\"]}" +
            "@{PinConfiguration.AccessCredential[\"" + host + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + port + "\"]}";
        // to sie dzieje w application.properties - tutaj jest zbedne

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
            case SINGLE:
            {
                if (id.isEmpty())
                    throw new IllegalArgumentException("Incorrect DataHandle.");
                try {
                    log.info("Downloading object with id: " + id);
                    //var filter = Builders<BsonDocument>.filter.eq("_id", new ObjectId(id));
                    Bson filter = eq("id", new ObjectId(id));
                    var document = mongoCollection.find(filter).first(); //firstOrDefault();
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
            case MULTIPLE:
            {
                try {
                    log.info("Downloading all files from " + collectionName);
                    localPath = localPath + "/" + collectionName;
                    File directory = new File(localPath);
                    if (!directory.exists()) {
                        throw new NotDirectoryException("Provided directory does not exist.");
                    }
                    var filter = empty();
                    for (BsonDocument bsonDocument : mongoCollection.find(filter))
                        DownloadSingleFile(bsonDocument, localPath);
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

        boolean isDirectory = new File(localPath).exists();
        if (pinConfiguration.dataMultiplicity.equals(DataMultiplicity.MULTIPLE) && !isDirectory)
            throw new IllegalArgumentException("Multiple data pin requires path pointing to a directory, not a file");
        if (pinConfiguration.dataMultiplicity.equals(DataMultiplicity.SINGLE) && isDirectory)
            throw new IllegalArgumentException("Single data pin requires path pointing to a file, not a directory");

        HashMap<String, String> handle = null;
        try {
            Pair<String, String> pair = prepare(null, null);
            String databaseName = pair.getValue0(), collectionName = pair.getValue1();

            switch (pinConfiguration.dataMultiplicity) {
                case SINGLE:
                {
                    log.info("Uploading file from " + localPath + " to collection " + collectionName);

                    var bsonDocument = getBsonDocument(localPath);
                    mongoCollection.insertOne(bsonDocument);

                    handle = getTokenHandle(bsonDocument);
                    handle.put("Database", databaseName);
                    handle.put("Collection", collectionName);

                    log.info("Upload file from " + localPath + " successful.");
                    break;
                }
                case MULTIPLE:
                {
                    log.info("Uploading directory from " + localPath + " to collection " + collectionName);
                    var files = getAllFiles(localPath);
                    var handleList = new ArrayList<HashMap<String, String>>();

                    for (String filePath : files) {
                        var bsonDocument = getBsonDocument(filePath);
                        mongoCollection.insertOne(bsonDocument);
                        handleList.add(getTokenHandle(bsonDocument));
                    }

                    handle = new HashMap<>();
                    handle.put("Files", new Gson().toJson(handleList));
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
            using var tcpClient = new TcpClient(); // zobacz to (powinno byc w application.prop) https://stackoverflow.com/questions/54742728/how-to-give-mongodb-socketkeepalive-in-spring-boot-application
            tcpClient.Connect(host, Integer.parseInt(port));
        } catch (Exception e) {
            log.error("Unable to reach " + host + ":" + port);
            return -1;
        }

        try {
            mongoClient = new MongoClientImpl(connectionString);
            mongoClient.listDatabases(); // Co ona robi w C#? https://csharp.hotexamples.com/examples/MongoDB.Driver/MongoClient/ListDatabases/php-mongoclient-listdatabases-method-examples.html
        } catch (MongoAuthenticationException e) {
            log.error("Unable to authenticate to MongoDB");
            return -2;
        } catch (Exception e) {
            log.error("Error " + e + " while trying to connect to MongoDB");
            return -1;
        }

        if (pinConfiguration.pinType.equals("input") && null != handle) {
            String databaseName, collectionName;
            String id = null;

            if (handle.containsKey("Database")) {
                databaseName = handle.get("Database");
            } else {
                throw new IllegalArgumentException("Incorrect DataHandle.");
            }

            if (handle.containsKey("Collection")) {
                collectionName = handle.get("Collection");
            } else {
                throw new IllegalArgumentException("Incorrect DataHandle.");
            }

            if (pinConfiguration.dataMultiplicity == DataMultiplicity.SINGLE &&
                    handle.containsKey("ObjectId")) {
                id = handle.get("ObjectId");
            } else {
                throw new IllegalArgumentException("Incorrect DataHandle.");
            }

            try {
                mongoDatabase = mongoClient.getDatabase(databaseName);
                if (mongoDatabase == null) {
                    log.error("No database " + databaseName);
                    return -3;
                }

                mongoCollection = mongoDatabase.getCollection(collectionName, BsonDocument.class);
                if (mongoCollection == null) {
                    log.error("No collection " + collectionName);
                    return -3;
                }

                if (pinConfiguration.dataMultiplicity == DataMultiplicity.SINGLE) {
                    var filter = eq("id", new ObjectId(id));
                    var document = mongoCollection.find(filter).first();
                    if (document == null) {
                        log.error("No document with id " + id);
                        return -3;
                    }
                }
            }
            catch (Exception e) {
                log.error("Error while trying to " +
                        (null != id ? ("get object " + id) : ("access collection " + collectionName)) +
                    " from database " + databaseName +
                    (null != id ? " from collection " + collectionName : ""));
                return -3;
            }
        }

        return 0;
    }

    private Pair<String, String> prepare(@Nullable String databaseName, @Nullable String collectionName) {
        if (databaseName == null) databaseName = "baltic_database_"+ UUID.randomUUID().toString().substring(0, 8);
        if (collectionName == null) collectionName = "baltic_collection_"+UUID.randomUUID().toString().substring(0, 8);
        //TODO to reset or not to reset
        mongoClient = MongoClients.create(connectionString);
        mongoDatabase = mongoClient.getDatabase(databaseName);
        mongoCollection = mongoDatabase.getCollection(collectionName, BsonDocument.class);
        return new Pair<>(databaseName, collectionName);
    }

    private static String DownloadSingleFile(BsonDocument document, String localPath) throws IOException {
        var fileName = document.get("fileName").asString();
        var fileContent = document.get("fileContent").asBinary();
        var filePath = localPath + "/" + fileName;
        return Files.write(
                Paths.get(filePath), fileContent.getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE).
                toString();
    }

    /*

    https://www.baeldung.com/spring-data-derived-queries
https://www.javappa.com/kurs-spring/spring-data-jpa-zapytania-wbudowane
     */

    private static BsonDocument getBsonDocument(String localPath) throws IOException {
        var objectId = ObjectId.get();
        var fileStream = new File(localPath);
        fileStream.setReadOnly();
        var fileName = fileStream.getName();
        var byteArray = Files.readAllBytes(Paths.get(localPath));

        var data = new HashMap<String, BsonValue>();
        data.put("id", new BsonObjectId(objectId));
        data.put("fileName", new BsonString(fileName));
        data.put("fileContent", new BsonBinary(byteArray));

        var bsonDocument = new BsonDocument();
        bsonDocument.putAll(data);
        return bsonDocument;
    }

    private static HashMap<String, String> getTokenHandle(BsonDocument document) {
        HashMap<String, String> map = new HashMap<>();
        map.put("FileName", String.valueOf(document.get("fileName").asString()));
        map.put("ObjectId", document.get("id").asObjectId().toString());
        return map;
    }
}
