package br.com.viabank.controller;
import br.com.viabank.dto.common.MessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    @PostMapping("/dashboard")
    MessageResponse sync(@RequestBody JsonNode ignored) {
        return new MessageResponse("Snapshot recebido apenas para compatibilidade. Dados financeiros enviados pelo front foram ignorados.");
    }
}
