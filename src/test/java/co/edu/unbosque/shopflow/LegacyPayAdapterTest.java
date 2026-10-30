package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import co.edu.unbosque.shopflow.pagos.PasarelaNoDisponibleException;
import co.edu.unbosque.shopflow.pagos.adaptadores.LegacyPayAdapter;

class LegacyPayAdapterTest {

	@Test
	void traduceElTimeoutDelSdkAUnaExcepcionDeShopFlow() {
		LegacyPayAdapter adapter = new LegacyPayAdapter(new LegacyPayClient(1.0)); // siempre falla

		assertThrows(PasarelaNoDisponibleException.class,
				() -> adapter.cobrar(new BigDecimal("100.00"), "PED-1"));
	}
}