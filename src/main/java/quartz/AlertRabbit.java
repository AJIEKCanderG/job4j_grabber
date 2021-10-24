package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author Ajiekcander
 */

public class AlertRabbit {

    /**
     * Read  file "rabbit.properties" and get interval.
     * @return interval seconds.
     */

    private static Properties readProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static int getInterval() {
       return Integer.parseInt(readProperties().getProperty("rabbit.interval"));
    }

    /**
     * Connecting to the database.
     * read database settings and connection.
     * @return connection
     */

    public static Connection initConnection() {
        Connection cn = null;
        try {
            Class.forName(readProperties().getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    readProperties().getProperty("url"),
                    readProperties().getProperty("username"),
                    readProperties().getProperty("password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cn;
    }

    /**
     * Create table rabbit with one field "created_date".
     */

    private static void createTable() throws SQLException {
        Connection cn = initConnection();
        String createTable = String.format("create table if not exists rabbit(%s);",
                "created_date timestamp");
        try (PreparedStatement ps = cn.prepareStatement(createTable)) {
            ps.execute();
        }
    }

    public static void main(String[] args) {
        try {
            Connection cn = initConnection();
            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            createTable();
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getInterval())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (var statement = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?)")) {
                statement.setDate(1, new Date(new java.util.Date().getTime()));
                statement.execute();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
