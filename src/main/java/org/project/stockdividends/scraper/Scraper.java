package org.project.stockdividends.scraper;

import org.project.stockdividends.model.Company;
import org.project.stockdividends.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
