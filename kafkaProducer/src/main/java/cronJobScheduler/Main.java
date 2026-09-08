package cronJobScheduler;

public class Main {
    public static void main(String[] args) {

        String fileName = "crontab.txt";

        ParseCronTab.startApp(fileName);
    }
}
