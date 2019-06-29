import com.google.gson.stream.JsonReader;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Transporter {
//    MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    private FileReader fileReader;

    public Transporter(String fileName) {
        try {
            fileReader = new FileReader(fileName);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void transportToDB() {
        try{
            BufferedReader bufferedReader  = new BufferedReader(fileReader);
            String line;
            String fileContent = "";
            while((line = bufferedReader.readLine())!=null){
                fileContent += line;
            }
            bufferedReader.close();
            System.out.println(fileContent);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

}

}
