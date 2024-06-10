package org.project.stockdividends.Service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.project.stockdividends.model.Company;
import org.project.stockdividends.model.ScrapedResult;
import org.project.stockdividends.persist.CompanyRepository;
import org.project.stockdividends.persist.DividendRepository;
import org.project.stockdividends.persist.entity.CompanyEntity;
import org.project.stockdividends.persist.entity.DividendEntity;
import org.project.stockdividends.scraper.Scraper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists) {
            throw new RuntimeException("Already exists ticker: " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
        // ticker 가 존재한다면 에러처리
        // 존재하지않는다면 storeCompanyAndDividend 함수 실행
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }
    private Company storeCompanyAndDividend(String ticker) {
        //ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("Failed to scrap ticker -> " + ticker);
        }
        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                                                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                    .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntityList);
        return company;


    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword,limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }
    // 저장
    public void addAutoCompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }
    // 검색
    public List<String> autoComplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }
    // 삭제
    public void deleteAutoCompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }


}
