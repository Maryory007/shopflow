package co.edu.unbosque.shopflow.facturacion.peru;

import java.math.BigDecimal;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;

public class GeneradorSUNAT implements GeneradorComprobante {


    @Override
    public Comprobante generar(
            String cliente,
            BigDecimal subtotal,
            BigDecimal impuesto
    ) {


        return new Comprobante(
                "Factura electrónica",
                "SUNAT",
                subtotal,
                impuesto,
                subtotal.add(impuesto),
                new BigDecimal("18")
        );

    }

}
