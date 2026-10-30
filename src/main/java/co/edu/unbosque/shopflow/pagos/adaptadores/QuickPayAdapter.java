package co.edu.unbosque.shopflow.pagos.adaptadores;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.edu.unbosque.shopflow.externos.quickpay.QuickPayRequest;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPayResult;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPaySdk;
import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;

/**
 * Adapter (GoF).
 * Target: PasarelaDePago · Adaptee: QuickPaySdk (SDK que no se puede modificar).
 * Traduce BigDecimal a double y el enum QuickPayStatus a ResultadoPago.
 */
public class QuickPayAdapter implements PasarelaDePago {

	private static final String MONEDA = "USD";

	private final QuickPaySdk sdk;

	public QuickPayAdapter() {
		this(new QuickPaySdk());
	}

	public QuickPayAdapter(QuickPaySdk sdk) {
		this.sdk = sdk;
	}

	@Override
	public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
		
		double monto = montoUsd.setScale(2, RoundingMode.HALF_UP).doubleValue();

		QuickPayResult resultado = sdk.pay(new QuickPayRequest(referencia, monto, MONEDA));

		return switch (resultado.status()) {
			case OK -> ResultadoPago.aprobado(resultado.transactionId());
			case REJECTED -> ResultadoPago.rechazado("Rechazado por QuickPay: " + resultado.detail());
			case ERROR -> ResultadoPago.rechazado("Error en QuickPay: " + resultado.detail());
		};
	}
}