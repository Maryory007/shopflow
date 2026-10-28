package co.edu.unbosque.shopflow.facturacion;


import java.math.BigDecimal;


public record Comprobante(

        String tipo,

        String descripcion,

        BigDecimal subtotal,

        BigDecimal impuesto,

        BigDecimal total,
        
        BigDecimal porcentajeIVA
        

) {

}