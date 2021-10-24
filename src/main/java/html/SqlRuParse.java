package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

/**
 * @author Ajiekcander
 */

public class SqlRuParse {

    public static void main(String[] args) throws Exception {
        String urlPost = "https://www.sql.ru/forum/1325330/lidy-be-fe-senior-cistemnye-analitiki-qa-i-devops-moskva-do-200t";
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
}
