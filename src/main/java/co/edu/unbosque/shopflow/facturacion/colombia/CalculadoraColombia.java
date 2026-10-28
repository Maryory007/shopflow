package co.edu.unbosque.shopflow.facturacion.colombia;


import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;


public class CalculadoraColombia 
        implements CalculadoraImpuestos {


    @Override
    public BigDecimal calcular(BigDecimal subtotal) {


        return subtotal.multiply(
                new BigDecimal("0.19")
        );

    }
}