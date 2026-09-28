package br.com.viabank.controller;
import br.com.viabank.dto.analytics.AnalyticsDtos.*;
import br.com.viabank.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AnalyticsController {
    private final AnalyticsService service;
    public AnalyticsController(AnalyticsService service) { this.service = service; }
    @GetMapping("/analytics/spending") SpendingResponse spending(@RequestParam(defaultValue="30d") String period) { return service.spending(period); }
    @GetMapping("/financial-health") FinancialHealthResponse health() { return service.health(); }
}
