package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import co.edu.unbosque.shopflow.facturacion.ServicioFacturacion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import co.edu.unbosque.shopflow.facturacion.Comprobante;

@Service
public class PedidoService {

	private final ServicioCatalogo servicioCatalogo;

	private final ServicioFacturacion servicioFacturacion;

	private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();

	private final AtomicInteger consecutivo = new AtomicInteger(1);

	public PedidoService(ServicioCatalogo servicioCatalogo, ServicioFacturacion servicioFacturacion) {

		this.servicioCatalogo = servicioCatalogo;
		this.servicioFacturacion = servicioFacturacion;

	}

	public Pedido crear(PedidoRequest request) {

		List<ItemPedido> items = request.items().stream().map(item -> {

			Producto producto = servicioCatalogo.buscar(item.sku());

			return new ItemPedido(producto.sku(), producto.nombre(), item.cantidad(), producto.precio());

		}).toList();

		String id = generarId();

		Pedido pedido = new Pedido.Builder(request.clienteId(), request.documentoCliente(), request.pais(),
				request.contacto(), items)

				.id(id).direccionEnvio(request.direccionEnvio()).recogerEnTienda(request.recogerEnTienda())
				.cupon(request.cupon()).notaRegalo(request.notaRegalo()).envioExpress(request.envioExpress())
				.canalNotificacion(request.canalNotificacion())

				.build();

		Comprobante comprobante = servicioFacturacion.facturar(pedido.getPais(), pedido.getDocumentoCliente(),
				pedido.getClienteId(), pedido.getSubtotal());

		pedido.asignarComprobante(comprobante);

		pedidos.put(id, pedido);

		return pedido;

	}

	public Pedido buscarPorId(String id) {

		Pedido pedido = pedidos.get(id);

		if (pedido == null) {

			throw new IllegalArgumentException("No existe el pedido " + id);

		}

		return pedido;

	}

	private BigDecimal calcularSubtotal(List<ItemPedido> items) {

		return items.stream().map(item -> item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

	}

	private BigDecimal calcularDescuento(String cupon, BigDecimal subtotal) {

		if ("BOSQUE10".equals(cupon)) {

			return subtotal.multiply(new BigDecimal("0.10"));

		}

		return BigDecimal.ZERO;

	}

	private String generarId() {

		String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		return String.format("PED-%s-%03d", fecha, consecutivo.getAndIncrement());

	}

}