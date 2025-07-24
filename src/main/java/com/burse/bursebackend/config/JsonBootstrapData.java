package com.burse.bursebackend.config;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JsonBootstrapData {
    private List<Stock> shares;
    private List<Trader> traders;
}

