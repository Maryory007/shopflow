package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.pedidos.ItemPedido;
import co.edu.unbosque.shopflow.pedidos.Pedido;

class PedidoBuilderTest {

	@Test
	void noDebeCrearPedidoSinItems() {

		Pedido.Builder builder = new Pedido.Builder("CLI-001", "12345678", "CO", "3001234567", List.of());

		assertThrows(IllegalArgumentException.class, () -> builder.build());

	}

	@Test
	void noDebeCrearPedidoDomicilioSinDireccion() {

		List<ItemPedido> items = List
				.of(new ItemPedido("SKU-001", "Teclado mecanico", 1, new java.math.BigDecimal("85.00")));

		Pedido.Builder builder = new Pedido.Builder("CLI-001", "12345678", "CO", "3001234567", items);

		builder.recogerEnTienda(false).envioExpress(false);

		assertThrows(IllegalArgumentException.class, () -> builder.build());

	}

}