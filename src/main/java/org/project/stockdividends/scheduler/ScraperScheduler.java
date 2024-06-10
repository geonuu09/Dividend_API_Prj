package org.project.stockdividends.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.stockdividends.model.Company;
import org.project.stockdividends.model.ScrapedResult;
import org.project.stockdividends.persist.CompanyRepository;
import org.project.stockdividends.persist.DividendRepository;
import org.project.stockdividends.persist.entity.CompanyEntity;
import org.project.stockdividends.persist.entity.DividendEntity;
import org.project.stockdividends.scraper.Scraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;

    // 일정 주기마다 수행
//    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        // 스크래퍼 실행됨을 로그로 확인.
         log.info("Starting yahoo finance scraper");

         // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            // 어느 회사가 스크래핑되었는지 확인 할 수 있음.
            log.info("Starting yahoo finance scraper ->{}", company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(Company.builder()
                                                                                .name(company.getName())
                                                                                .ticker(company.getTicker())
                                                                                .build());

            // 스크래핑한 배당금 정보 중 DB 에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // 디비든 모델을 디비든 엔티티로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 하나씩 디비든 엔티티로 매핑
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.
                                                existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                        }
                    });
            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}