package org.project.stockdividends.scraper;

import org.hibernate.annotations.Comment;
import org.hibernate.sql.exec.spi.StandardEntityInstanceResolver;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.project.stockdividends.model.Company;
import org.project.stockdividends.model.Dividend;
import org.project.stockdividends.model.ScrapedResult;
import org.project.stockdividends.model.constants.Month;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/?period1=%d&period2=%d&interval=1mo&filter=history&frequency=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400; // 1일 : 60초 * 60분 * 24시간 =86400

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);
        try {
            long now = System.currentTimeMillis() / 1000;
            String URL = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            // Jsoup을 사용하여 URL에 연결하고 문서를 가져옵니다.
            Connection connection = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            Document document = connection.get();
            Elements ParsingDivs = document.getElementsByAttributeValueContaining("class", "table");
            Element tableEle = ParsingDivs.get(1);

            Element tbody = tableEle.select("tbody").first();
            List<Dividend> dividends = new ArrayList<>();

            for (Element tr : tbody.select("tr")) {
                String text = tr.text();
                if (!text.endsWith("Dividend")) {
                    continue;
                }

                String[] split = text.split(" ");
                int month = Month.strToNumber(split[0]);
                int day = Integer.valueOf(split[1].replace(",", ""));
                int year = Integer.valueOf(split[2]);
                String dividend = split[3];

                if (month < 0) {
                    throw new RuntimeException("Invalid month: " + split[0]);
                }

                dividends.add(Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend)
                        .build());

            }

            scrapResult.setDividends(dividends);


        } catch (IOException e) {
            // 예외가 발생할 경우 스택 트레이스를 출력합니다.
            e.printStackTrace();
        }
        return scrapResult;
    }
    @Override
    public Company scrapCompanyByTicker(String ticker){
        String url = String.format(SUMMARY_URL,ticker,ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(1);
//            String title = titleEle.text().split(" - ")[1].trim();
            String title = titleEle.text();
            // abc - def - zyx => split(" - ") 일 때마다 자른다 -> 배열로 반환이 됨.
            // 반환된 배열에 [1]를 가져온다. 가져온 요소의 앞 뒤의 공백을 모두 없앤다. (trim)
            // yahoo finance 에서 ticker 를 제공해주는 템플릿이 저렇기 때문에, 우리가 사용하고자 하는 방식으로 변형
            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

}
