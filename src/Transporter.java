import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mongodb.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Transporter {
    private FileReader fileReader;
    private MongoClient mongoClient;
    private DB db;
    private DBCollection dbCollection;

    public Transporter(String fileName) {
        try {
            fileReader = new FileReader(fileName);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        setCredentials();
    }

    private void setCredentials() {
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String fileContent = "";
            while ((line = bufferedReader.readLine()) != null) {
                fileContent += line;
            }
            bufferedReader.close();
            System.out.println(fileContent);
            JsonParser jsonParser = new JsonParser();
            JsonElement creds = jsonParser.parse(fileContent);
            JsonObject credsObj = creds.getAsJsonObject();
            String userName = credsObj.get("user_name").getAsString();
            String psw = credsObj.get("psw").getAsString();
            String host = credsObj.get("host").getAsString();
            String port = credsObj.get("port").getAsString();
            String database = credsObj.get("db").getAsString();

            String clientArgs = "mongodb://" + userName + ":" + psw + "@" + host + ":" + port + "/" + database;
            mongoClient = new MongoClient(new MongoClientURI(clientArgs));
            db = mongoClient.getDB(database);
            dbCollection = db.getCollection("mentions");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void transportToDB() {


    }

    public void testWrite() {
        DBObject mention = new BasicDBObject("_id", "jo")
                .append("name", "Jo Bloggs")
                .append("address", new BasicDBObject("street", "123 Fake St")
                        .append("city", "Faketon")
                        .append("state", "MA")
                        .append("zip", 12345));
        WriteResult w  = dbCollection.insert(mention);
        System.out.println(w.toString());
    }
}
