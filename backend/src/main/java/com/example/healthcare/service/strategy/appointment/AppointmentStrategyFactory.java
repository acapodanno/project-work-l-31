package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AppointmentStrategyFactory {

    private final Map<AppointmentStatus, AppointmentStatusStrategy> strategies;

    public AppointmentStrategyFactory(List<AppointmentStatusStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(AppointmentStatusStrategy::getSupportedStatus, Function.identity()));
    }

    public AppointmentStatusStrategy getStrategy(AppointmentStatus status) {
        return strategies.get(status);
    }
}
