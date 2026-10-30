package co.edu.unbosque.shopflow.pagos.adaptadores;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayTimeoutException;
import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.PasarelaNoDisponibleException;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;

/**
 * Adapter (GoF).
 * Target: PasarelaDePago · Adaptee: LegacyPayClient (SDK que no se puede modificar).
 * Traduce montos (dólares a centavos), respuestas ("00|LP4051") y excepciones.
 */
public class LegacyPayAdapter implements PasarelaDePago {

	private static final String MONEDA = "USD";
	private static final String CODIGO_APROBADO = "00";
	private static final String CODIGO_FONDOS_INSUFICIENTES = "51";

	private final LegacyPayClient cliente;

	
	public LegacyPayAdapter(double probabilidadTimeout) {
		this(new LegacyPayClient(probabilidadTimeout));
	}


	public LegacyPayAdapter(LegacyPayClient cliente) {
		this.cliente = cliente;
	}

	@Override
	public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
		long centavos = aCentavos(montoUsd);
		String respuesta;
		try {
			respuesta = cliente.makeCharge(centavos, MONEDA, referencia);
		} catch (LegacyPayTimeoutException e) {
			throw new PasarelaNoDisponibleException("LegacyPay no respondió a tiempo", e);
		}
		return interpretar(respuesta);
	}

	
	private long aCentavos(BigDecimal montoUsd) {
		return montoUsd.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
	}


	private ResultadoPago interpretar(String respuesta) {
		String[] partes = respuesta.split("\\|", -1);
		String codigo = partes[0];
		String autorizacion = partes.length > 1 ? partes[1] : "";

		if (CODIGO_APROBADO.equals(codigo)) {
			return ResultadoPago.aprobado(autorizacion);
		}
		if (CODIGO_FONDOS_INSUFICIENTES.equals(codigo)) {
			return ResultadoPago.rechazado("Fondos insuficientes");
		}
		return ResultadoPago.rechazado("Transacción no permitida (código " + codigo + ")");
	}
}