package grabber;

import date.SqlRuDateTimeParser;
import model.Post;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Ajiekcander
 */

public class PsqlStore implements Store, AutoCloseable {

    private final Connection cn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Метод сохраняет выбранный пост в базу данных в таблицу post
     *
     * @param post принимаемый пост
     */
    @Override
    public void save(Post post) {
        try (var statement = cn.prepareStatement(
                "insert into post (name, text, link, created) values (?, ?, ?, ?);", Statement
                        .RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getName());
            statement.setString(2, post.getText());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод показывает все посты из базы данных из таблицы post
     *
     * @return возвращает лист всех постов
     */
    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        try (var statement = cn.prepareStatement(
                "select * from post;")) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rsl.add(createPost(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    /**
     * Метод показывает объявление из базы данных с указанным id
     *
     * @param id принимаемый на входе id поста
     * @return возвращает найденный по id пост
     */
    @Override
    public Post findById(int id) {
        var sql = "select * from post where id = ?";
        Post post = null;
        try (var statement = cn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (var rslKey = statement.executeQuery()) {
                if (rslKey.next()) {
                    post = createPost(rslKey);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cn != null) {
            cn.close();
        }
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream("./src/main/resources/app.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) throws SQLException, IOException {
        Parse parse = new SqlRuParse(new SqlRuDateTimeParser());
        Store store = new PsqlStore(getProperties());
        List<Post> posts = parse.list("https://www.sql.ru/forum/job-offers/1");
        posts.forEach(store::save);
        List<Post> postsFromDb = store.getAll();
        postsFromDb.forEach(System.out::println);
        if (postsFromDb.size() > 0) {
            Post postFromDb = store.findById(postsFromDb.get(0).getId());
            System.out.println(postFromDb);
        }
    }
}