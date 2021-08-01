package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService service;
    @Mock
    private LeilaoDao leilaoDao;
    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.service = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
    }

    @Test
    public void deveriaFinalizaLeilao() {
        List<Leilao> leiloes = listaLeiloesExpirados();
        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Assertions.assertTrue(leilao.isFechado());
        Assertions.assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());

        Mockito.verify(leilaoDao).salvar(leilao);
    }

    @Test
    public void deveriaEnviarEmail() {
        List<Leilao> leiloes = listaLeiloesExpirados();
        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Lance lanceVencedor = leilao.getLanceVencedor();
        Mockito.verify(enviadorDeEmails).enviarEmailVencedorLeilao(lanceVencedor);
    }

    @Test
    public void naoDeveriaEnviarEmailEmCasoDeException() {
        List<Leilao> leiloes = listaLeiloesExpirados();
        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);
        Mockito.when(leilaoDao.salvar(Mockito.any()))
                .thenThrow(RuntimeException.class);

        try {
            service.finalizarLeiloesExpirados();
            Mockito.verifyNoInteractions(enviadorDeEmails);
        } catch (Exception e) {
        }
    }

    private List<Leilao> listaLeiloesExpirados() {
        List<Leilao> lista = new ArrayList<>();
        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("User 1"));
        Lance lance1 = new Lance(new Usuario("User 2"), new BigDecimal("600"));
        Lance lance2 = new Lance(new Usuario("User 3"), new BigDecimal("900"));

        leilao.propoe(lance1);
        leilao.propoe(lance2);

        lista.add(leilao);
        return lista;
    }
}
