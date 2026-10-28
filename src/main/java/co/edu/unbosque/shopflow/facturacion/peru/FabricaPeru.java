package co.edu.unbosque.shopflow.facturacion.peru;

import org.springframework.stereotype.Component;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import co.edu.unbosque.shopflow.facturacion.FabricaFacturacion;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

@Component
public class FabricaPeru implements FabricaFacturacion {

	@Override
	public String pais() {

		return "PE";

	}

	@Override
	public CalculadoraImpuestos crearCalculadora() {

		return new CalculadoraPeru();

	}

	@Override
	public ValidadorIdentificacion crearValidador() {

		return new ValidadorPeru();

	}

	@Override
	public GeneradorComprobante crearGenerador() {

		return new GeneradorSUNAT();

	}

}