package co.edu.unbosque.shopflow.facturacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ServicioFacturacion {

	private final Map<String, FabricaFacturacion> fabricas;

	public ServicioFacturacion(List<FabricaFacturacion> listaFabricas) {

		this.fabricas = listaFabricas.stream()
				.collect(Collectors.toMap(fabrica -> fabrica.pais(), Function.identity()));
	}

	public Comprobante facturar(String pais, String documento, String cliente, BigDecimal subtotal) {

		FabricaFacturacion fabrica = fabricas.get(pais.toUpperCase());

		if (fabrica == null) {

			throw new IllegalArgumentException("No existe configuración de facturación para " + pais);
		}

		boolean valido = fabrica.crearValidador().validar(documento);

		if (!valido) {

			throw new IllegalArgumentException("Documento inválido para " + pais);
		}

		BigDecimal impuesto = fabrica.crearCalculadora().calcular(subtotal);

		return fabrica.crearGenerador().generar(cliente, subtotal, impuesto);
	}

}
