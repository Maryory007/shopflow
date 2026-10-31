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
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;
import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.notificaciones.Notificador;

@Service
public class PedidoService {

	private final ServicioCatalogo servicioCatalogo;

	private final ServicioFacturacion servicioFacturacion;
	
	private final Map<String, PasarelaDePago> pasarelas;

	private final Map<String, Notificador> notificadores;

	private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();

	private final AtomicInteger consecutivo = new AtomicInteger(1);
	
	public PedidoService(ServicioCatalogo servicioCatalogo, ServicioFacturacion servicioFacturacion,
			@Qualifier("pasarelas") Map<String, PasarelaDePago> pasarelas, List<Notificador> notificadores) {

		this.servicioCatalogo = servicioCatalogo;
		this.servicioFacturacion = servicioFacturacion;
		this.pasarelas = pasarelas;
		this.notificadores = notificadores.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
				notificador -> notificador.canal().toUpperCase(), notificador -> notificador));

	}

	public Pedido crear(PedidoRequest request) {
		String canal = request.canalNotificacion() == null || request.canalNotificacion().isBlank()
				? "EMAIL" : request.canalNotificacion().toUpperCase();
		if (!notificadores.containsKey(canal)) {
			throw new IllegalArgumentException("Canal de notificación no soportado: " + canal);
		}

		List<ItemPedido> items = request.items().stream().map(item -> {

			Producto producto = servicioCatalogo.buscar(item.sku());

			return new ItemPedido(producto.sku(), producto.nombre(), item.cantidad(), producto.precio());

		}).toList();

		String id = generarId();

		Pedido pedido = new Pedido.Builder(request.clienteId(), request.documentoCliente(), request.pais(),
				request.contacto(), items)

				.id(id).direccionEnvio(request.direccionEnvio()).recogerEnTienda(request.recogerEnTienda())
				.cupon(request.cupon()).notaRegalo(request.notaRegalo()).envioExpress(request.envioExpress())
				.canalNotificacion(canal)

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
	
	public Pedido pagar(String id, String proveedor) {

		Pedido pedido = buscarPorId(id);

		if (pedido.estaPagado()) {

			throw new IllegalStateException("El pedido " + id + " ya fue pagado");

		}

		PasarelaDePago pasarela = obtenerPasarela(proveedor);

		ResultadoPago resultado = pasarela.cobrar(pedido.getComprobante().total(), pedido.getId());

		if (!resultado.aprobado()) {

			throw new IllegalStateException(resultado.mensaje());

		}

		pedido.registrarPago(resultado.autorizacion());

		Notificador notificador = notificadores.get(pedido.getCanalNotificacion());
		notificador.notificarPagoAprobado(pedido.getContacto(), pedido.getId(), pedido.getComprobante().total());

		return pedido;

	}

	private PasarelaDePago obtenerPasarela(String proveedor) {

		PasarelaDePago pasarela = (proveedor == null) ? null : pasarelas.get(proveedor.toUpperCase());

		if (pasarela == null) {

			throw new IllegalArgumentException("Proveedor de pago no soportado: " + proveedor + ". Use LEGACYPAY o QUICKPAY");

		}

		return pasarela;

	}

}
