package co.edu.unbosque.shopflow.pagos;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unbosque.shopflow.pagos.adaptadores.LegacyPayAdapter;
import co.edu.unbosque.shopflow.pagos.adaptadores.QuickPayAdapter;

/**
 * Punto de composición de los pagos: el único lugar que conoce las clases concretas.
 * Entrega las pasarelas listas para usar, indexadas por proveedor.
 */
@Configuration
public class ConfiguracionPagos {

	@Bean
	public Map<String, PasarelaDePago> pasarelas(
			@Value("${shopflow.legacypay.probabilidad-timeout}") double probabilidadTimeout) {

		return Map.of(
				"LEGACYPAY", new LegacyPayAdapter(probabilidadTimeout),
				"QUICKPAY", new QuickPayAdapter());
	}
}