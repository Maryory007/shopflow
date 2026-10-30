package co.edu.unbosque.shopflow.pagos.decoradores;

import java.math.BigDecimal;

import co.edu.unbosque.shopflow.pagos.EntradaAuditoria;
import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.PasarelaNoDisponibleException;
import co.edu.unbosque.shopflow.pagos.RegistroAuditoria;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;

/**
 * Decorador: mide la duración del cobro, delega y registra el resultado.
 * Registra también cuando la pasarela no responde, y luego deja pasar la excepción.
 */
public class AuditoriaPago extends PasarelaDecorator {

	private final String proveedor;

	private final RegistroAuditoria registro;

	public AuditoriaPago(PasarelaDePago envuelta, String proveedor, RegistroAuditoria registro) {
		super(envuelta);
		this.proveedor = proveedor;
		this.registro = registro;
	}

	@Override
	public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
		long inicio = System.currentTimeMillis();
		try {
			ResultadoPago resultado = envuelta.cobrar(montoUsd, referencia);
			registrar(montoUsd, referencia, resultado.aprobado() ? "APROBADO" : "RECHAZADO",
					resultado.mensaje(), inicio);
			return resultado;
		} catch (PasarelaNoDisponibleException e) {
			registrar(montoUsd, referencia, "NO DISPONIBLE", e.getMessage(), inicio);
			throw e;
		}
	}

	private void registrar(BigDecimal monto, String referencia, String resultado, String mensaje, long inicio) {
		long duracionMs = System.currentTimeMillis() - inicio;
		registro.registrar(new EntradaAuditoria(proveedor, referencia, monto, resultado, mensaje, duracionMs));
	}
}