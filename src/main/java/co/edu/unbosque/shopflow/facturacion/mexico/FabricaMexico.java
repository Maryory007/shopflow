package co.edu.unbosque.shopflow.facturacion.mexico;

import org.springframework.stereotype.Component;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import co.edu.unbosque.shopflow.facturacion.FabricaFacturacion;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

@Component
public class FabricaMexico implements FabricaFacturacion {

	@Override
	public String pais() {

		return "MX";
	}

	@Override
	public CalculadoraImpuestos crearCalculadora() {

		return new CalculadoraMexico();
	}

	@Override
	public ValidadorIdentificacion crearValidador() {

		return new ValidadorMexico();
	}

	@Override
	public GeneradorComprobante crearGenerador() {

		return new GeneradorCFDI();
	}

}
