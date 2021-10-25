package grabber;

import model.Post;
import java.util.List;

/**
 * @author Ajiekcander
 */

public interface Store {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
