package co.edu.unbosque.shopflow.catalogo;

import java.math.BigDecimal;
import java.util.List;

import co.edu.unbosque.shopflow.comun.AccesoDenegadoException;

public class CatalogoProteccionProxy implements ServicioCatalogo {

    private final ServicioCatalogo catalogo;
    private final String rol;

    public CatalogoProteccionProxy(ServicioCatalogo catalogo, String rol) {
        this.catalogo = catalogo;
        this.rol = rol;
    }

    @Override
    public Producto buscar(String sku) {
        return catalogo.buscar(sku);
    }

    @Override
    public List<Producto> listar() {
        return catalogo.listar();
    }

    @Override
    public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
        if (!"ADMIN".equalsIgnoreCase(rol)) {
            throw new AccesoDenegadoException("Solo el rol ADMIN puede cambiar precios");
        }
        if (nuevoPrecio == null || nuevoPrecio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return catalogo.actualizarPrecio(sku, nuevoPrecio);
    }
}
