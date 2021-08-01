package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.*;

public class GeradorDePagamentoTest {

    private GeradorDePagamento geradorDePagamento;
    @Mock
    private PagamentoDao pagamentoDao;
    @Mock
    private Clock clock;
    @Captor
    private ArgumentCaptor<Pagamento> captor;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.geradorDePagamento = new GeradorDePagamento(this.pagamentoDao, clock);
    }

    @Test
    public void deveriaCriarPagamento() {
        Leilao leilao = leilaoFake();
        Lance lanceVencedor = leilao.getLanceVencedor();

        LocalDate date = LocalDate.of(2021, Month.JULY, 30);
        Mockito.when(clock.instant())
                .thenReturn(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        geradorDePagamento.gerarPagamento(lanceVencedor);

        Mockito.verify(pagamentoDao).salvar(captor.capture());
        Pagamento pagamento = captor.getValue();
        Assertions.assertEquals(LocalDate.of(2021, Month.AUGUST, 2), pagamento.getVencimento());
        Assertions.assertEquals(leilao, pagamento.getLeilao());
        Assertions.assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        Assertions.assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        Assertions.assertFalse(pagamento.getPago());
    }

    private Leilao leilaoFake() {
        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("User 1"));
        Lance lance = new Lance(new Usuario("User 3"), new BigDecimal("900"));

        leilao.propoe(lance);
        leilao.setLanceVencedor(lance);

        return leilao;
    }
}
