package com.burse.bursebackend.services.stocks;

import java.util.Set;

public interface IStockPriceUpdater {
    public void updateAllPrices();
    public void setStrategyByName(String name);
    public String getCurrentStrategyName();
    public Set<String> getAvailableStrategyNames();


}
