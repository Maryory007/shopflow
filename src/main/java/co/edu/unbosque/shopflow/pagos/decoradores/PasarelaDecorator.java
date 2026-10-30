package co.edu.unbosque.shopflow.pagos.decoradores;

import java.math.BigDecimal;

import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;

/**
 * Decorator (GoF): decorador base.
 * ES una PasarelaDePago (implementa la interfaz) y TIENE una PasarelaDePago (la envuelta).
 * Por defecto delega; cada decorador concreto agrega su comportamiento antes o después.
 */
public abstract class PasarelaDecorator implements PasarelaDePago {

	protected final PasarelaDePago envuelta;

	protected PasarelaDecorator(PasarelaDePago envuelta) {
		this.envuelta = envuelta;
	}

	@Override
	public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
		return envuelta.cobrar(montoUsd, referencia);
	}
}