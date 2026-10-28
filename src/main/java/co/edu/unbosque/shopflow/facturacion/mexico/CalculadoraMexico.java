package co.edu.unbosque.shopflow.facturacion.mexico;


import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;


public class CalculadoraMexico 
        implements CalculadoraImpuestos {


    @Override
    public BigDecimal calcular(BigDecimal subtotal) {


        return subtotal.multiply(
                new BigDecimal("0.16")
        );

    }
}