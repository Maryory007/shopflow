package co.edu.unbosque.shopflow.catalogo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionCatalogo {

    @Bean
    public ServicioCatalogo servicioCatalogo() {
        return new CatalogoCacheProxy(new CatalogoRemoto());
    }
}
