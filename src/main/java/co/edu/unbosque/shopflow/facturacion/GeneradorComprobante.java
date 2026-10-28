package co.edu.unbosque.shopflow.facturacion;

import java.math.BigDecimal;

public interface GeneradorComprobante {

	Comprobante generar(String cliente, BigDecimal subtotal, BigDecimal impuesto);

}
