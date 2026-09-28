package br.com.viabank.controller;

import br.com.viabank.dto.transaction.TransactionResponse;
import br.com.viabank.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service) { this.service = service; }

    @GetMapping
    Map<String,Object> list(@RequestParam(defaultValue="30d") String period,
                            @RequestParam(defaultValue="1") int page,
                            @RequestParam(defaultValue="50") int limit) {
        Page<TransactionResponse> result = service.list(period, page, limit);
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("transactions", result.getContent());
        response.put("page", result.getNumber()+1);
        response.put("totalPages", result.getTotalPages());
        response.put("totalElements", result.getTotalElements());
        return response;
    }

    @GetMapping("/{id}") TransactionResponse get(@PathVariable Long id) { return service.get(id); }
}
