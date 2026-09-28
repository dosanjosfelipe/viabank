package br.com.viabank.controller;
import br.com.viabank.dto.common.MessageResponse;
import br.com.viabank.dto.device.DeviceRequest;
import br.com.viabank.service.DeviceService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;
    public DeviceController(DeviceService service) { this.service = service; }
    @PostMapping ResponseEntity<MessageResponse> register(@RequestBody DeviceRequest request) {
        service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Dispositivo registrado."));
    }
}
