package co.edu.unbosque.shopflow.facturacion.colombia;

import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

public class ValidadorColombia 
        implements ValidadorIdentificacion {


    @Override
    public boolean validar(String documento) {


        return documento != null
                && documento.matches("\\d{6,10}");
    }

}
