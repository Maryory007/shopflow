package co.edu.unbosque.shopflow.facturacion.mexico;

import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;


public class GeneradorCFDI 
        implements GeneradorComprobante {


    @Override
    public Comprobante generar(
            String cliente,
            BigDecimal subtotal,
            BigDecimal impuesto
    ) {


        return new Comprobante(
                "CFDI",
                "SAT",
                subtotal,
                impuesto,
                subtotal.add(impuesto),
                new BigDecimal("16")
        
        );
    }
}