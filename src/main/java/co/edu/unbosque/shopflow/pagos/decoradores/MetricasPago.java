package co.edu.unbosque.shopflow.pagos.decoradores;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;

/**
 * Decorador (reto B): cuenta los cobros y los aprobados de una pasarela
 * y escribe en consola la tasa de aprobación después de cada cobro.
 */
public class MetricasPago extends PasarelaDecorator {

	private static final Locale ES_CO = Locale.forLanguageTag("es-CO");

	private final String proveedor;

	private final AtomicInteger cobros = new AtomicInteger();

	private final AtomicInteger aprobados = new AtomicInteger();

	public MetricasPago(PasarelaDePago envuelta, String proveedor) {
		super(envuelta);
		this.proveedor = proveedor;
	}

	@Override
	public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
		try {
			ResultadoPago resultado = envuelta.cobrar(montoUsd, referencia);
			if (resultado.aprobado()) {
				aprobados.incrementAndGet();
			}
			return resultado;
		} finally {
			int total = cobros.incrementAndGet();
			int ok = aprobados.get();
			double tasa = 100.0 * ok / total;
			System.out.println(String.format(ES_CO,
					"[Métricas] %s · cobros: %d · aprobados: %d · tasa de aprobación: %.1f %%",
					proveedor, total, ok, tasa));
		}
	}
}