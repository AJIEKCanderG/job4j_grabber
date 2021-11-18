package grabber;

import date.SqlRuDateTimeParser;
import model.Post;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Properties;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private final Properties cfg = new Properties();

    /**
     * Метод создает объект класса PsqlStore, передает в него cfg для инициализации.
     *
     * @return возвращает готовый объект PsqlStore.
     */
    public Store store() {
        return new PsqlStore(cfg);
    }

    /**
     * Метод создает scheduler()
     *
     * @return возвращаемый scheduler()
     * @throws SchedulerException исключение при ошибке создания.
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    /**
     * Метод считывает файл app.properties
     *
     * @throws IOException исключение ввода/ввывода
     */
    public void cfg() throws IOException {
        try (InputStream in = new FileInputStream("./src/main/resources/app.properties")) {
            cfg.load(in);
        }
    }

    /**
     * Метод инициализирует переменные и создает работу по расписанию
     *
     * @param parse входной объект Parse
     * @param store входной объект Store
     * @param scheduler входной шедулер
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        /**
         * Метод выполняет созданную работу
         *
         * @param context контекст работы
         */
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            try {
                for (Post post : parse.list("https://www.sql.ru/forum/job-offers/")) {
                        store.save(post);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Ответ от сервера в виде строкового представления списка найденных вакансий
     * @param store список вакансий из бд
     */
    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(new SqlRuDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}