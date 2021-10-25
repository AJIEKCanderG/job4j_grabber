package grabber;

import model.Post;

import java.io.IOException;
import java.util.List;

/**
 * @author Ajiekcander
 */

public interface Parse {
    List<Post> list(String link) throws IOException;

    Post detail(String link) throws IOException;
}