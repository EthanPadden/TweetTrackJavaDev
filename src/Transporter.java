import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mongodb.*;
import twitter4j.Status;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Transporter {
    private FileReader fileReader;
    private MongoClient mongoClient;
    private DB db;
    private DBCollection dbCollection;
    JsonParser jsonParser;

    public Transporter(String fileName) {
        try {
            fileReader = new FileReader(fileName);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        setCredentials();
        jsonParser = new JsonParser();
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

    public boolean writeToDb(Status status, String handle) {
        DBObject mention = new BasicDBObject("handle", handle)
                .append("tweeting_user", status.getUser().getScreenName())
                .append("tweet_id", status.getId())
                .append("created_at", status.getCreatedAt().toString())
                .append("text", status.getText());

        WriteResult w  = dbCollection.insert(mention);
        JsonObject result = jsonParser.parse(w.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }
}
