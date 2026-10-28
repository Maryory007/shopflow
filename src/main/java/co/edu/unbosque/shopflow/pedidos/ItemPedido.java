package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;

public record ItemPedido(
        String sku,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario

) {}
