package com.setcom.computation.dataaccess;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.lang.Nullable;
import com.setcom.computation.balticlsc.DataHandle;
import com.setcom.computation.datamodel.DataMultiplicity;
import lombok.extern.slf4j.Slf4j; //TODO #6 Remove Lombok
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

@Slf4j
public class MongoDbHandle extends DataHandle {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<BsonDocument> mongoCollection;
    private final String connectionString;
    @Value("${spring.data.mongodb.username}")
    private String user;
    @Value("${spring.data.mongodb.password}")
    private String password;
    @Value("${spring.data.mongodb.host}")
    private String host;
    @Value("${spring.data.mongodb.port}")
    private String port;

    public MongoDbHandle(String pinName, JSONObject configuration) {
        super(pinName, configuration);
        connectionString = "mongodb://{PinConfiguration.AccessCredential[\"" + user + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + password + "\"]}" +
            "@{PinConfiguration.AccessCredential[\"" + host + "\"]}" +
            ":{PinConfiguration.AccessCredential[\"" + port + "\"]}";
    }

    @Override
    public String download(HashMap<String, String> handle) throws Exception {
        String databaseName = handle.getOrDefault("Database", "");
        String collectionName = handle.getOrDefault("Collection", "");
        if (!pinConfiguration.pinType.equals("input"))
            throw new Exception("Download cannot be called for output pins");
        if (databaseName.isEmpty())
            throw new IllegalArgumentException("Incorrect DataHandle.");
        if (collectionName.isEmpty())
            throw new IllegalArgumentException("Incorrect DataHandle.");

        prepare(databaseName, collectionName);

        String localPath = "";
        String id = handle.getOrDefault("ObjectId", "");
        switch (pinConfiguration.dataMultiplicity) {
            case SINGLE: {
                if (id.isEmpty())
                    throw new IllegalArgumentException("Incorrect DataHandle.");
                try {
                    log.info("Downloading object with id: " + id);
                    Bson filter = eq("id", new ObjectId(id));
                    var document = mongoCollection.find(filter).first();
                    if (document != null) {
                        localPath = downloadSingleFile(document, this.localPath);
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
            } case MULTIPLE: {
                try {
                    log.info("Downloading all files from " + collectionName);
                    localPath = localPath + "/" + collectionName;
                    File directory = new File(localPath);
                    if (!directory.exists()) {
                        throw new NotDirectoryException("Provided directory does not exist.");
                    }
                    var filter = empty();
                    for (BsonDocument bsonDocument : mongoCollection.find(filter))
                        downloadSingleFile(bsonDocument, localPath);
                    addGuidToFilesName(localPath);
                    log.info("Downloading all files from " + collectionName + " successful.");
                } catch (Exception e) {
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
                case SINGLE: {
                    log.info("Uploading file from " + localPath + " to collection " + collectionName);

                    var bsonDocument = getBsonDocument(localPath);
                    mongoCollection.insertOne(bsonDocument);

                    handle = getTokenHandle(bsonDocument);
                    handle.put("Database", databaseName);
                    handle.put("Collection", collectionName);

                    log.info("Upload file from " + localPath + " successful.");
                    break;
                }
                case MULTIPLE: {
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
        } catch (Exception e) {
            log.error("Error: " + e + "\n Uploading from " + localPath +  " failed.");
            throw e;
        } finally {
            clearLocal();
        }
    }

    public short checkConnection(@Nullable Map<String, String> handle) {
        String host = pinConfiguration.accessCredential.get("Host"),
                port = pinConfiguration.accessCredential.get("Port");

        try {
            new Socket(host, Integer.parseInt(port));
        } catch (Exception e) {
            log.error("Unable to reach " + host + ":" + port);
            return -1;
        }

        try {
            mongoClient = new MongoClient(connectionString);
            mongoClient.listDatabases();
        } catch (Exception e) {
            log.error("Error " + e + " while trying to connect to Mongodb");
            return -1;
        }

        if (pinConfiguration.pinType.equals("input") && null != handle) {
            String databaseName, collectionName;
            String id;

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
                mongoCollection = mongoDatabase.getCollection(collectionName, BsonDocument.class);

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
        mongoClient = new MongoClient(connectionString);
        mongoDatabase = mongoClient.getDatabase(databaseName);
        mongoCollection = mongoDatabase.getCollection(collectionName, BsonDocument.class);
        return new Pair<>(databaseName, collectionName);
    }

    private static String downloadSingleFile(BsonDocument document, String localPath) throws IOException {
        var fileName = document.get("fileName").asString();
        var fileContent = document.get("fileContent").asBinary();
        var filePath = localPath + "/" + fileName;
        return Files.write(
                Paths.get(filePath), fileContent.getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE).
                toString();
    }

    /*
    //TODO #7 zobaczyć czy to w ogóle jest przydatne
    https://www.baeldung.com/spring-data-derived-queries
    https://www.javappa.com/kurs-spring/spring-data-jpa-zapytania-wbudowane
     */

    private static BsonDocument getBsonDocument(String localPath) throws IOException {
        var objectId = ObjectId.get();
        var fileStream = new File(localPath);
        if (!fileStream.setReadOnly()) {
            log.error("Filed to set file as ReadOnly");
        }
        var fileName = fileStream.getName();
        var byteArray = Files.readAllBytes(Paths.get(localPath));

        Map<String, BsonValue> data = new HashMap<>();
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
