package br.com.viabank.controller;
import br.com.viabank.dto.card.CardDtos.*;
import br.com.viabank.service.CardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService service;
    public CardController(CardService service) { this.service = service; }
    @GetMapping List<CardResponse> list() { return service.list(); }
    @GetMapping("/{id}") CardResponse get(@PathVariable Long id) { return service.get(id); }
    @PatchMapping("/{id}/status") CardResponse status(@PathVariable Long id, @RequestBody CardStatusRequest request) { return service.setBlocked(id, request); }
    @PatchMapping("/{id}/limit") CardResponse limit(@PathVariable Long id, @Valid @RequestBody CardLimitRequest request) { return service.updateLimit(id, request); }
    @GetMapping("/{id}/virtual") VirtualCardResponse virtual(@PathVariable Long id) { return service.virtual(id); }
}
