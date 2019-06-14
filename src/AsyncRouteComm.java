import java.util.List;
import java.util.Scanner;


public class AsyncRouteComm {
    private Scanner reader;

    public AsyncRouteComm() {
        reader = new Scanner(System.in);
    }

    public void asyncOutput(List<Object> list) {
        int signal = reader.nextInt();
        int i = 0;

        if(signal == 0) {
            System.out.println(list.get(i));
            i++;
        }
    }

}
