package co.edu.unbosque.shopflow.pedidos;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {

		this.pedidoService = pedidoService;

	}

	@PostMapping
	public ResponseEntity<?> crear(@RequestBody PedidoRequest request) {

		try {

			Pedido pedido = pedidoService.crear(request);

			return ResponseEntity.status(HttpStatus.CREATED).body(pedido);

		} catch (IllegalArgumentException e) {

			return ResponseEntity.badRequest().body(e.getMessage());

		}

	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscar(@PathVariable String id) {

		try {

			return ResponseEntity.ok(pedidoService.buscarPorId(id));

		} catch (IllegalArgumentException e) {

			return ((BodyBuilder) ResponseEntity.notFound()).body(e.getMessage());

		}

	}

}