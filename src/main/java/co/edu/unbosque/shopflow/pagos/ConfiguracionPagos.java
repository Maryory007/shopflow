package co.edu.unbosque.shopflow.pagos;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unbosque.shopflow.pagos.adaptadores.LegacyPayAdapter;
import co.edu.unbosque.shopflow.pagos.adaptadores.QuickPayAdapter;
import co.edu.unbosque.shopflow.pagos.decoradores.AuditoriaPago;
import co.edu.unbosque.shopflow.pagos.decoradores.ReintentoPago;

/**
 * Punto de composición de los pagos: el único lugar que conoce las clases concretas.
 * Construye cada adapter, lo envuelve en capas según las propiedades
 * y entrega las pasarelas armadas, indexadas por proveedor.
 */
@Configuration
public class ConfiguracionPagos {

	@Value("${shopflow.pagos.reintentos}")
	private boolean reintentos;

	@Value("${shopflow.pagos.max-intentos}")
	private int maxIntentos;

	@Value("${shopflow.pagos.auditoria}")
	private boolean auditoria;

	@Bean
	public Map<String, PasarelaDePago> pasarelas(
			@Value("${shopflow.legacypay.probabilidad-timeout}") double probabilidadTimeout,
			RegistroAuditoria registro) {

		return Map.of(
				"LEGACYPAY", armar("LEGACYPAY", new LegacyPayAdapter(probabilidadTimeout), registro),
				"QUICKPAY", armar("QUICKPAY", new QuickPayAdapter(), registro));
	}


	private PasarelaDePago armar(String proveedor, PasarelaDePago adapter, RegistroAuditoria registro) {
		PasarelaDePago pasarela = adapter;

		if (reintentos) {
			pasarela = new ReintentoPago(pasarela, maxIntentos);
		}
		if (auditoria) {
			pasarela = new AuditoriaPago(pasarela, proveedor, registro);
		}

		return pasarela;
	}
}