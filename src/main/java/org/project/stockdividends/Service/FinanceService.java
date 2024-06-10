package org.project.stockdividends.Service;

import lombok.AllArgsConstructor;
import org.project.stockdividends.model.Company;
import org.project.stockdividends.model.Dividend;
import org.project.stockdividends.model.ScrapedResult;
import org.project.stockdividends.persist.CompanyRepository;
import org.project.stockdividends.persist.DividendRepository;
import org.project.stockdividends.persist.entity.CompanyEntity;
import org.project.stockdividends.persist.entity.DividendEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;


    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명"));

        // 2. 조회된 회사ID로 배당금 정보 조회
        List< DividendEntity> dividendEntities = this.dividendRepository.findByCompanyId(company.getId());
        // 3. 결과 조합 후 반환

//        방법 1
//        List<Dividend> dividends = new ArrayList<>();
//        for(var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }
//          방법 2
        List<Dividend> dividends = dividendEntities.stream()
                .map(e-> Dividend.builder()
                        .date(e.getDate())
                        .dividend(e.getDividend())
                        .build())
                        .collect(Collectors.toList());


        return new ScrapedResult(Company.builder()
                .ticker(company.getTicker())
                .name(company.getName())
                .build(),dividends);
    }
}
