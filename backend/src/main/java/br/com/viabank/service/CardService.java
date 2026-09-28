package br.com.viabank.service;

import br.com.viabank.dto.card.CardDtos.*;
import br.com.viabank.entity.Card;
import br.com.viabank.exception.*;
import br.com.viabank.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CardService {
    private final AccountService accountService;
    private final CardRepository cards;
    public CardService(AccountService accountService, CardRepository cards) {
        this.accountService = accountService; this.cards = cards;
    }

    public List<CardResponse> list() {
        Long accountId = accountService.account().getId();
        return cards.findAllByAccountIdOrderByIdAsc(accountId).stream().map(this::map).toList();
    }

    public CardResponse get(Long id) { return map(owned(id)); }

    @Transactional
    public CardResponse setBlocked(Long id, CardStatusRequest request) {
        Card card = owned(id); card.setBlocked(request.blocked()); return map(cards.save(card));
    }

    @Transactional
    public CardResponse updateLimit(Long id, CardLimitRequest request) {
        Card card = owned(id);
        BigDecimal newLimit = AccountService.money(request.limit());
        if (newLimit.compareTo(card.getUsedLimit()) < 0)
            throw new BusinessException("O limite não pode ser menor que o valor já utilizado.");
        if (newLimit.compareTo(new BigDecimal("100000.00")) > 0)
            throw new BusinessException("Limite solicitado acima do permitido para este ambiente.");
        card.setLimitAmount(newLimit);
        return map(cards.save(card));
    }

    public VirtualCardResponse virtual(Long id) {
        Card card = owned(id);
        return new VirtualCardResponse(card.getId(), card.getBrand(), card.getLast4(),
            card.getExpiration(), "Dados sensíveis do cartão não são expostos pela API.");
    }

    private Card owned(Long id) {
        Long accountId = accountService.account().getId();
        return cards.findByIdAndAccountId(id, accountId)
            .orElseThrow(() -> new NotFoundException("Cartão não encontrado."));
    }

    private CardResponse map(Card card) {
        BigDecimal available = card.getLimitAmount().subtract(card.getUsedLimit()).max(BigDecimal.ZERO);
        return new CardResponse(card.getId(), card.getType().toLowerCase(), card.getBrand(), card.getLast4(),
            card.getHolderName(), card.getExpiration(), card.isBlocked(),
            card.getLimitAmount(), card.getUsedLimit(), available, card.getProductName());
    }
}
