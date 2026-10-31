package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.catalogo.CatalogoCacheProxy;
import co.edu.unbosque.shopflow.catalogo.CatalogoProteccionProxy;
import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import co.edu.unbosque.shopflow.comun.AccesoDenegadoException;

class CatalogoProxyTest {

    @Test
    void cacheaUnaConsultaYActualizaElPrecioEnCache() {
        AtomicInteger llamadas = new AtomicInteger();
        ServicioCatalogo remoto = new ServicioCatalogo() {
            private Producto producto = new Producto("SKU-1", "Teclado", new BigDecimal("10.00"));

            @Override
            public Producto buscar(String sku) {
                llamadas.incrementAndGet();
                return producto;
            }

            @Override
            public List<Producto> listar() {
                return List.of(producto);
            }

            @Override
            public Producto actualizarPrecio(String sku, BigDecimal precio) {
                producto = producto.conPrecio(precio);
                return producto;
            }
        };
        CatalogoCacheProxy proxy = new CatalogoCacheProxy(remoto);

        assertEquals(new BigDecimal("10.00"), proxy.buscar("SKU-1").precio());
        assertEquals(new BigDecimal("10.00"), proxy.buscar("SKU-1").precio());
        assertEquals(1, llamadas.get());
        proxy.actualizarPrecio("SKU-1", new BigDecimal("12.50"));
        assertEquals(new BigDecimal("12.50"), proxy.buscar("SKU-1").precio());
        assertEquals(1, llamadas.get());
    }

    @Test
    void cacheaElProductoListadoParaLasBusquedasPosteriores() {
        AtomicInteger llamadas = new AtomicInteger();
        ServicioCatalogo remoto = new ServicioCatalogo() {
            private final Producto producto = new Producto("SKU-2", "Mouse", new BigDecimal("5.00"));

            @Override
            public Producto buscar(String sku) {
                llamadas.incrementAndGet();
                throw new NoSuchElementException(sku);
            }

            @Override
            public List<Producto> listar() {
                return List.of(producto);
            }

            @Override
            public Producto actualizarPrecio(String sku, BigDecimal precio) {
                return producto.conPrecio(precio);
            }
        };
        CatalogoCacheProxy proxy = new CatalogoCacheProxy(remoto);

        proxy.listar();

        assertEquals(new BigDecimal("5.00"), proxy.buscar("SKU-2").precio());
        assertEquals(0, llamadas.get());
    }

    @Test
    void soloAdminPuedeActualizarYElCambioSePropagaAlCatalogo() {
        ServicioCatalogo remoto = new ServicioCatalogo() {
            private Producto producto = new Producto("SKU-3", "Monitor", new BigDecimal("100.00"));

            @Override
            public Producto buscar(String sku) {
                return producto;
            }

            @Override
            public List<Producto> listar() {
                return List.of(producto);
            }

            @Override
            public Producto actualizarPrecio(String sku, BigDecimal precio) {
                producto = producto.conPrecio(precio);
                return producto;
            }
        };
        CatalogoCacheProxy cache = new CatalogoCacheProxy(remoto);
        CatalogoProteccionProxy cliente = new CatalogoProteccionProxy(cache, "CLIENTE");

        assertThrows(AccesoDenegadoException.class,
                () -> cliente.actualizarPrecio("SKU-3", new BigDecimal("90.00")));
        new CatalogoProteccionProxy(cache, "ADMIN").actualizarPrecio("SKU-3", new BigDecimal("90.00"));
        assertEquals(new BigDecimal("90.00"), cache.buscar("SKU-3").precio());
    }
}
