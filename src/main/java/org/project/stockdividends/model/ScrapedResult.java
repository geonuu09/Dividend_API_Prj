package org.project.stockdividends.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {
    private Company company;

    // 배당긂 리스트
    private List<Dividend> dividends;

    public ScrapedResult() {this.dividends = new ArrayList<>();}

}
