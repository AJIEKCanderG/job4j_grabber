package grabber;

import model.Post;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Ajiekcander
 */

public interface Store {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id) throws SQLException;
}
