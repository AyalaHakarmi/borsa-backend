package com.burse.bursebackend.dtos.offer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BaseOfferDTO {

    private String id;

    private BigDecimal price;

    private int amount;

    private String traderId;

    private String stockId;
}
