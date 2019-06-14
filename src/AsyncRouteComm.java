import java.util.List;
import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AsyncRouteComm {
    private Scanner reader;
    private int i;

    public AsyncRouteComm() {
        reader = new Scanner(System.in);
        i = 0;
    }

    public void asyncOutput(List<JsonObject> list) {
        System.out.println("w");
        int signal = reader.nextInt();

        if(signal == 0) {
            System.out.println("Tweet: " + list.get(i).toString());
            i++;
            asyncOutput(list);
        }
    }

}
