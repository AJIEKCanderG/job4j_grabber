package html;

import date.DateTimeParser;
import grabber.Parse;
import model.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ajiekcander
 */

public class SqlRuParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws Exception {
        String urlPost = "https://www.sql.ru/forum/1325330/"
                + "lidy-be-fe-senior-cistemnye-analitiki-qa-i-devops-moskva-do-200t";
        for (int i = 1; i <= 5; i++) {
            String url = "https://www.sql.ru/forum/job-offers/";
            Document doc = Jsoup.connect(url + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                System.out.println(href.attr("href"));
                System.out.println(href.text());
                Element date = td.parent().child(5);
                System.out.println(date.text());
            }
            System.out.println("\n" + "---------------------------- End page "
                    + i + " ----------------------------------" + "\n");
        }
        postParse(urlPost);
    }

    public static void postParse(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element message = doc.select(".msgBody").get(1);
        Elements date = doc.select(".msgFooter");
        String mesText = message.text();
        String dateText = date.first().ownText().replace(" [] |", "");
        System.out.println("---------------------------- postParse "
                + "----------------------------------");
        System.out.println(mesText);
        System.out.println(dateText);
    }

    /**
     * Парсит список всех постов
     * @param link ссылка на страницу
     * @return возвращает список всех постов со страницы
     */

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        Document doc = Jsoup.connect(link).get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            String  attr = href.attr("href");
            postList.add(detail(attr));
        }
        return postList;
    }

    /**
     * Загружает все детали одного поста (имя, описание, дату создания, ссылку на пост).
     * @param link ссылка на страницу
     * @return модель с данными.
     */

    @Override
    public Post detail(String link) throws IOException {
        Post post = new Post();
        Document doc = Jsoup.connect(link).get();
        Elements postMessageHeader = doc.select(".messageHeader");
        Element postDescription = doc.select(".msgBody").get(1);
        Elements postDateCreated = doc.select(".msgFooter");
        String title = postMessageHeader.first().text().trim();
        String description = postDescription.ownText().trim();
        String dateCreated = postDateCreated.first().ownText().replace(" [] |", "");
        LocalDateTime parseDate = dateTimeParser.parse(dateCreated);
        post.setTitle(title);
        post.setDescription(description);
        post.setLink(link);
        post.setCreated(parseDate);
        return post;
    }
}
