package com.example.healthcare.service.strategy.ticket;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TicketStrategyFactory {

    private final Map<String, TicketStatusStrategy> strategies;

    public TicketStrategyFactory(List<TicketStatusStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(TicketStatusStrategy::getSupportedStatus, Function.identity()));
    }

    public TicketStatusStrategy getStrategy(String status) {
        return strategies.get(status.toUpperCase());
    }
}
