package co.edu.unbosque.shopflow.pagos;

/** Cuerpo de POST /api/pedidos/{id}/pago: {"proveedor": "LEGACYPAY"} o {"proveedor": "QUICKPAY"}. */
public record PagoRequest(String proveedor) {
}