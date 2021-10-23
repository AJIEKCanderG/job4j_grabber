package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    private static int readPropertiesGetInterval() {
        int interval = 0;
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
            interval = Integer.parseInt(config.getProperty("rabbit.interval"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return interval;
    }


    /**
     * Connecting to the database.
     * read database settings and connection.
     * @return connection
     */

    public static Connection initConnection() {
        Connection cn;
        try (InputStream in = AlertRabbit.class.
                getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
        Connection cn = initConnection();
        try {
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
                    .withIntervalInSeconds(readPropertiesGetInterval())
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
            try (var statement = connection.createStatement()) {
                statement.execute(
                        "INSERT INTO rabbit (created_date) VALUES (current_timestamp)"
                );
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
