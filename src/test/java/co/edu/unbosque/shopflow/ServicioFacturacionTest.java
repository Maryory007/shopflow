package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.facturacion.ServicioFacturacion;
import co.edu.unbosque.shopflow.facturacion.colombia.FabricaColombia;
import co.edu.unbosque.shopflow.facturacion.mexico.FabricaMexico;
import co.edu.unbosque.shopflow.facturacion.peru.FabricaPeru;
import co.edu.unbosque.shopflow.pedidos.ItemPedido;
import co.edu.unbosque.shopflow.pedidos.Pedido;

class ServicioFacturacionTest {

	private ServicioFacturacion servicio;

	@BeforeEach
	void preparar() {

		servicio = new ServicioFacturacion(List.of(new FabricaColombia(), new FabricaMexico(), new FabricaPeru()));

	}

	@Test
	void mexicoNoDebeAceptarCedulaColombiana() {

		List<ItemPedido> items = List.of(new ItemPedido("SKU-001", "Teclado mecanico", 1, new BigDecimal("85.00")));

		Pedido pedido = new Pedido.Builder("CLI001", "1020304050", "MX", "3001234567", items).id("PED-001")
				.direccionEnvio("Ciudad de Mexico").recogerEnTienda(false).envioExpress(true).build();

		assertThrows(IllegalArgumentException.class, () -> servicio.facturar(pedido.getPais(),
				pedido.getDocumentoCliente(), pedido.getClienteId(), pedido.getSubtotal()));

	}

}