import java.util.List;
import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AsyncRouteComm {
    private Scanner reader;

    public AsyncRouteComm() {
        reader = new Scanner(System.in);
    }

    public void asyncOutput(List<JsonObject> list) {
        int signal = reader.nextInt();
        int i = 0;

        if(signal == 0) {
//            System.out.println(list.get(i));
//            System.out.println("Tweet: " + i + statusJson.toString());

            i++;
        }
    }

}
