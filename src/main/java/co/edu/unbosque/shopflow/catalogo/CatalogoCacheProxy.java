package co.edu.unbosque.shopflow.catalogo;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CatalogoCacheProxy implements ServicioCatalogo {

    private final ServicioCatalogo catalogoReal;
    private final ConcurrentMap<String, Producto> cache = new ConcurrentHashMap<>();

    public CatalogoCacheProxy(ServicioCatalogo catalogoReal) {
        this.catalogoReal = catalogoReal;
    }

    @Override
    public Producto buscar(String sku) {
        return cache.computeIfAbsent(sku, catalogoReal::buscar);
    }

    @Override
    public List<Producto> listar() {
        List<Producto> productos = catalogoReal.listar();
        productos.forEach(producto -> cache.put(producto.sku(), producto));
        return productos;
    }

    @Override
    public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
        Producto actualizado = catalogoReal.actualizarPrecio(sku, nuevoPrecio);
        cache.put(sku, actualizado);
        return actualizado;
    }
}
