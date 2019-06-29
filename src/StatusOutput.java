

        import java.io.FileNotFoundException;
        import java.io.PrintWriter;
        import java.util.Date;
        import java.util.Timer;
        import java.util.TimerTask;

public class StatusOutput extends TimerTask {

    PrintWriter printWriter;

    public StatusOutput() {
        try {
            this.printWriter = new PrintWriter("trackermsg.txt");

        } catch (FileNotFoundException e) {
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
//        try {
        printWriter.println("Alive at: " + new Date().toString());
        System.out.println("Alive at: " + new Date().toString());
        printWriter.flush();

        //assuming it takes 10 secs to complete the task
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}


