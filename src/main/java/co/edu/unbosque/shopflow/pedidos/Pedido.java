package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;
import java.util.List;

import co.edu.unbosque.shopflow.facturacion.Comprobante;

public class Pedido {

	private final String id;

	private final String clienteId;

	private final String documentoCliente;

	private final String pais;

	private final String contacto;

	private final List<ItemPedido> items;

	private final String direccionEnvio;

	private final Boolean recogerEnTienda;

	private final String cupon;

	private final String notaRegalo;

	private final Boolean envioExpress;

	private final String canalNotificacion;

	private final BigDecimal subtotal;

	private final BigDecimal descuento;

	private Comprobante comprobante;

	private String estado = "CREADO";

	private String autorizacionPago;

	private Pedido(Builder builder) {

		this.id = builder.id;
		this.clienteId = builder.clienteId;
		this.documentoCliente = builder.documentoCliente;
		this.pais = builder.pais;
		this.contacto = builder.contacto;
		this.items = builder.items;
		this.direccionEnvio = builder.direccionEnvio;
		this.recogerEnTienda = builder.recogerEnTienda;
		this.cupon = builder.cupon;
		this.notaRegalo = builder.notaRegalo;
		this.envioExpress = builder.envioExpress;
		this.canalNotificacion = builder.canalNotificacion;
		this.subtotal = builder.subtotal;
		this.descuento = builder.descuento;


	}

	public static class Builder {

		private String id;

		private final String clienteId;

		private final String documentoCliente;

		private final String pais;

		private final String contacto;

		private final List<ItemPedido> items;

		private String direccionEnvio;

		private Boolean recogerEnTienda = false;

		private String cupon;

		private String notaRegalo;

		private Boolean envioExpress = false;

		private String canalNotificacion;

		private BigDecimal subtotal = BigDecimal.ZERO;

		private BigDecimal descuento = BigDecimal.ZERO;

		public Builder(String clienteId, String documentoCliente, String pais, String contacto,
				List<ItemPedido> items) {

			this.clienteId = clienteId;
			this.documentoCliente = documentoCliente;
			this.pais = pais;
			this.contacto = contacto;
			this.items = items;

		}

		public Builder id(String id) {

			this.id = id;
			return this;

		}

		public Builder direccionEnvio(String direccionEnvio) {

			this.direccionEnvio = direccionEnvio;
			return this;

		}

		public Builder recogerEnTienda(Boolean recogerEnTienda) {

			this.recogerEnTienda = recogerEnTienda;
			return this;

		}

		public Builder cupon(String cupon) {

			this.cupon = cupon;
			return this;

		}

		public Builder notaRegalo(String notaRegalo) {

			this.notaRegalo = notaRegalo;
			return this;

		}

		public Builder envioExpress(Boolean envioExpress) {

			this.envioExpress = envioExpress;
			return this;

		}

		public Builder canalNotificacion(String canalNotificacion) {

			this.canalNotificacion = canalNotificacion;
			return this;

		}

		private void calcularSubtotal() {

			subtotal = items.stream().map(item -> item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);

		}

		private void calcularDescuento() {

			descuento = BigDecimal.ZERO;

			if ("BOSQUE10".equalsIgnoreCase(cupon)) {

				descuento = subtotal.multiply(new BigDecimal("0.10"));

			}

			subtotal = subtotal.subtract(descuento);

		}

		public Pedido build() {

			if (items == null || items.isEmpty()) {

				throw new IllegalArgumentException("El pedido debe tener al menos un item");

			}

			if (!Boolean.TRUE.equals(recogerEnTienda) && (direccionEnvio == null || direccionEnvio.isBlank())) {

				throw new IllegalArgumentException("La dirección es obligatoria para pedidos a domicilio");

			}

			if (Boolean.TRUE.equals(recogerEnTienda) && Boolean.TRUE.equals(envioExpress)) {

				throw new IllegalArgumentException("Un pedido para recoger en tienda no puede ser express");

			}

			if (notaRegalo != null && notaRegalo.length() > 140) {

				throw new IllegalArgumentException("La nota de regalo no puede superar 140 caracteres");

			}

			calcularSubtotal();

			calcularDescuento();

			return new Pedido(this);

		}

	}

	public boolean estaPagado() {

		return "PAGADO".equals(estado);

	}

	public void registrarPago(String autorizacion) {

		if (estaPagado()) {

			throw new IllegalStateException("El pedido " + id + " ya fue pagado");

		}

		this.estado = "PAGADO";
		this.autorizacionPago = autorizacion;

	}
	public String getEstado() {
		return estado;
	}

	public String getAutorizacionPago() {
		return autorizacionPago;
	}

	public void asignarComprobante(Comprobante comprobante) {

		this.comprobante = comprobante;

	}

	public String getId() {
		return id;
	}

	public String getClienteId() {
		return clienteId;
	}

	public String getDocumentoCliente() {
		return documentoCliente;
	}

	public String getPais() {
		return pais;
	}

	public String getContacto() {
		return contacto;
	}

	public List<ItemPedido> getItems() {
		return items;
	}

	public String getDireccionEnvio() {
		return direccionEnvio;
	}

	public Boolean getRecogerEnTienda() {
		return recogerEnTienda;
	}

	public String getCupon() {
		return cupon;
	}

	public String getNotaRegalo() {
		return notaRegalo;
	}

	public Boolean getEnvioExpress() {
		return envioExpress;
	}

	public String getCanalNotificacion() {
		return canalNotificacion;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getDescuento() {
		return descuento;
	}

	public Comprobante getComprobante() {
		return comprobante;
	}

}