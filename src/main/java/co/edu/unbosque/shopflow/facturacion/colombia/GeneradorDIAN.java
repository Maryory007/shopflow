package co.edu.unbosque.shopflow.facturacion.colombia;
import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;



public class GeneradorDIAN 
        implements GeneradorComprobante {


    @Override
    public Comprobante generar(
            String cliente,
            BigDecimal subtotal,
            BigDecimal impuesto
    ) {


        return new Comprobante(
                "Factura electrónica",
                "DIAN",
                subtotal,
                impuesto,
                subtotal.add(impuesto),
                new BigDecimal("19")
        
        );
    }
}