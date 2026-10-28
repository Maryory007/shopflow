package co.edu.unbosque.shopflow.facturacion.peru;

import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

public class ValidadorPeru implements ValidadorIdentificacion {


    @Override
    public boolean validar(String documento) {


        return documento != null
                && (
                    documento.matches("\\d{8}")
                    ||
                    documento.matches("\\d{11}")
                );

    }

}