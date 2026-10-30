package co.edu.unbosque.shopflow.pagos;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.shopflow.pedidos.Pedido;
import co.edu.unbosque.shopflow.pedidos.PedidoService;

/**
 * Endpoints de pago. Los errores (400, 409, 503) los traduce ManejadorErrores.
 */
@RestController
public class PagoController {

	private final PedidoService pedidoService;

	private final RegistroAuditoria registroAuditoria;

	public PagoController(PedidoService pedidoService, RegistroAuditoria registroAuditoria) {
		this.pedidoService = pedidoService;
		this.registroAuditoria = registroAuditoria;
	}

	@PostMapping("/api/pedidos/{id}/pago")
	public Pedido pagar(@PathVariable String id, @RequestBody PagoRequest request) {
		return pedidoService.pagar(id, request.proveedor());
	}

	@GetMapping("/api/pagos/auditoria")
	public List<EntradaAuditoria> auditoria() {
		return registroAuditoria.listar();
	}
}