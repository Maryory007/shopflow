package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;

/** Una línea de la auditoría de pagos: un cobro con su resultado y duración. */
public record EntradaAuditoria(String proveedor, String referencia, BigDecimal monto, String resultado,
		String mensaje, long duracionMs) {
}