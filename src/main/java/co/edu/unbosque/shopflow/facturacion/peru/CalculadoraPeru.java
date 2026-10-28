package co.edu.unbosque.shopflow.facturacion.peru;

import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;

public class CalculadoraPeru implements CalculadoraImpuestos {


    @Override
    public BigDecimal calcular(BigDecimal subtotal) {


        return subtotal.multiply(
                new BigDecimal("0.18")
        );

    }
}