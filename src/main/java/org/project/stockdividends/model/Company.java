package org.project.stockdividends.model;

import lombok.Builder;
import lombok.Data;

@Data
// getter, setter, 여러 어노테이션을 포함하는 어노테이션
@Builder
// 디자인 패턴 중 빌더 패턴을 사용할 수 있게 하는 어노테이션


public class Company {
    private String ticker;
    private String name;
}

