package br.com.viabank.controller;
import br.com.viabank.dto.common.MessageResponse;
import br.com.viabank.dto.pix.PixDtos.*;
import br.com.viabank.service.PixService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pix")
public class PixController {
    private final PixService service;
    public PixController(PixService service) { this.service = service; }

    @GetMapping("/keys") List<PixKeyResponse> keys() { return service.keys(); }

    @PostMapping("/keys")
    ResponseEntity<PixKeyResponse> createKey(@Valid @RequestBody CreateKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createKey(request));
    }

    @DeleteMapping("/keys/{id}")
    MessageResponse deleteKey(@PathVariable Long id) {
        service.deleteKey(id); return new MessageResponse("Chave PIX removida.");
    }

    @PostMapping("/send") PixTransferResponse send(@Valid @RequestBody SendPixRequest request) {
        return service.send(request);
    }

    @GetMapping("/favorites") List<FavoriteResponse> favorites() { return service.favorites(); }

    @PostMapping("/favorites")
    ResponseEntity<FavoriteResponse> addFavorite(@Valid @RequestBody AddFavoriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addFavorite(request));
    }
}
