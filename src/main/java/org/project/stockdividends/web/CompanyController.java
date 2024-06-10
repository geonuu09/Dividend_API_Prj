package org.project.stockdividends.web;

import lombok.AllArgsConstructor;
import org.project.stockdividends.Service.CompanyService;
import org.project.stockdividends.model.Company;
import org.project.stockdividends.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private CompanyService companyService;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocompleteCompany(@RequestParam String keyword) {
        var result = this.companyService.getCompanyNamesByKeyword(keyword);

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("Ticker is empty");
        }
        Company company = this.companyService.save(ticker);
        this.companyService.addAutoCompleteKeyword(company.getName());

        return ResponseEntity.ok(company);

    }

    @DeleteMapping
    public ResponseEntity<?> deleteCompany() {
        return null;
    }
}
