package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.PasarelaNoDisponibleException;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;
import co.edu.unbosque.shopflow.pagos.decoradores.ReintentoPago;

class ReintentoPagoTest {

	@Test
	void reintentaHastaQueLaPasarelaResponde() {
		int[] llamadas = { 0 };
		PasarelaDePago inestable = (monto, ref) -> { // pasarela falsa
			if (++llamadas[0] < 3) throw new PasarelaNoDisponibleException("caída", null);
			return ResultadoPago.aprobado("AUT-1");
		};

		ResultadoPago r = new ReintentoPago(inestable, 3).cobrar(BigDecimal.TEN, "PED-1");

		assertTrue(r.aprobado());
		assertEquals(3, llamadas[0]);
	}
}