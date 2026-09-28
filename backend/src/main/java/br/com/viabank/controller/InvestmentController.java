package br.com.viabank.controller;
import br.com.viabank.dto.investment.InvestmentDtos.*;
import br.com.viabank.service.InvestmentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {
    private final InvestmentService service;
    public InvestmentController(InvestmentService service) { this.service = service; }
    @GetMapping("/products") List<ProductResponse> products() { return service.products(); }
    @GetMapping("/portfolio") PortfolioResponse portfolio() { return service.portfolio(); }
    @PostMapping ResponseEntity<InvestmentResponse> invest(@Valid @RequestBody InvestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.invest(request));
    }
    @PostMapping("/{id}/redeem") InvestmentResponse redeem(@PathVariable Long id, @Valid @RequestBody RedeemRequest request) {
        return service.redeem(id, request);
    }
}
