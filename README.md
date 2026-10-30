# ShopFlow

Ingeniería de Software 2 · Taller: patrones de diseño creacionales y estructurales en una API REST (Universidad El Bosque, 2026-2).

---

## 1. Integrantes, roles y ejecución del proyecto

### Integrantes y roles

| Rol | Integrante | Patrones | Reto de extensión |
|---|---|---|---|
| A | Maryory González Rendón | Builder → Abstract Factory | A · Perú (Abstract Factory) |
| B | Andrés Felipe González | Adapter → Decorator | B · Métricas (Decorator) |
| C | Juan David González | Proxy → Factory Method | C · WhatsApp (Factory Method) |

Integrador de las ramas: Andrés Felipe González

### Cómo ejecutar el proyecto

Requisitos: Java 17 o superior y Maven (o un IDE con soporte para Maven).

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.



## 2. Tabla de análisis de patrones (Anexo D)

| Patrón | Problema en ShopFlow (síntoma) | Participantes GoF → clases | Principio SOLID | Cuándo no usarlo |
|---|---|---|---|---|
| **Factory Method** | _Pendiente (Rol C)_ | | | |
| **Abstract Factory** | La facturación depende del país. Cada país tiene reglas distintas de impuestos, validación de identificación y comprobante. Con lógica condicional por país (`if`/`switch`), cada país nuevo obliga a modificar el servicio de facturación y se pueden mezclar reglas (por ejemplo, validar un pedido de México con reglas colombianas). | **Abstract Factory:** `FabricaFacturacion`.<br>**Concrete Factory:** `FabricaColombia`, `FabricaMexico`, `FabricaPeru`.<br>**Abstract Products:** `CalculadoraImpuestos`, `ValidadorIdentificacion`, `GeneradorComprobante`.<br>**Concrete Products:** `CalculadoraColombia`, `ValidadorColombia`, `GeneradorDIAN`; `CalculadoraMexico`, `ValidadorMexico`, `GeneradorCFDI`; `CalculadoraPeru`, `ValidadorPeru`, `GeneradorSUNAT`.<br>**Client:** `ServicioFacturacion`. | **OCP:** un país nuevo es una fábrica nueva, sin modificar `ServicioFacturacion`.<br>**DIP:** el servicio depende de la abstracción `FabricaFacturacion`, no de las clases de cada país. | Cuando no hay familias (un solo país) o cuando la familia cambia con frecuencia: agregar un producto nuevo a la familia obliga a modificar la interfaz y todas las fábricas concretas. |
| **Builder** | Un pedido tiene 4 datos obligatorios, al menos un ítem y 6 opciones (dirección, recoger en tienda, cupón, nota de regalo, envío express, canal de notificación), con reglas que dependen unas de otras. Un constructor largo permite confundir el orden de dos `String`; con setters, el pedido existe incompleto y puede quedar inválido. | **Builder:** `Pedido.Builder`.<br>**Product:** `Pedido` (inmutable, constructor privado).<br>**Director:** `PedidoService.crear()`, que traduce el JSON recibido a llamadas al builder. | **SRP:** el producto representa el pedido y el builder se encarga de construirlo y validarlo.<br>**OCP:** agregar una opción nueva no obliga a crear más constructores. | Para objetos con dos o tres parámetros, todos obligatorios: el builder duplica código sin aportar nada y hay que mantenerlo cada vez que cambian los atributos. |
| **Adapter** | Dos proveedores con SDK que no se pueden modificar y no coinciden entre sí ni con ShopFlow. Sin patrón, centavos, códigos "00…" y enums quedarían regados por la lógica de negocio. | <br>**Target:** `PasarelaDePago` <br>**Adapters:** `LegacyPayAdapter`, `QuickPayAdapter`. <br>**Adaptees:** `LegacyPayClient`, `QuickPaySdk`. <br>**Client:** `PedidoService`.| <br>**SRP:** la traducción de montos, respuestas y excepciones vive en una sola clase por proveedor.<br>**OCP:** un proveedor nuevo es un adapter nuevo, sin tocar PedidoService. |Cuando se controlan ambas interfaces: es más simple alinearlas. Y cuando la intención es simplificar un subsistema completo: eso es Facade. |
| **Decorator** | LegacyPay falla con timeout el 30 % de las veces, así que conviene reintentar; auditoría exige registrar cada cobro de los dos proveedores, y en pruebas los reintentos deben poder apagarse. Con herencia harían falta seis subclases (dos proveedores por tres combinaciones, como `LegacyPayConReintentoYAuditoria`), y cada capa nueva duplicaría la cuenta. | **Component:** `PasarelaDePago`.<br>Concrete Component: `LegacyPayAdapter`, `QuickPayAdapterv`.<br>**Decorator:** `PasarelaDecorator` (abstracta).<br>**Concrete Decorators:** `ReintentoPago`, `AuditoriaPago`, `MetricasPago`. | **OCP:** una capa nueva es una clase nueva; solo se toca ConfiguracionPagos para agregarla.<br>SRP: cada decorador hace una sola cosa (reintentar, auditar o medir).<br>LSP: una pasarela decorada se usa donde se espera cualquier PasarelaDePago. | Cuando las combinaciones son pocas y fijas (dos subclases bastan), cuando el cliente depende de los tipos concretos, o cuando una capa necesita saber qué otras capas hay en la cadena. Muchas capas dificultan la depuración. |
| **Proxy** | _Pendiente (Rol C)_ | | | |

---

## 3. Patrones implementados

### 3.1 Builder

#### Problema que resuelve

Un pedido tiene datos obligatorios (cliente, país, documento, contacto y al menos un ítem) y muchas opciones. Varias reglas dependen de más de una opción a la vez:

- La dirección es obligatoria si el pedido no se recoge en tienda.
- Un pedido para recoger en tienda no puede ser express.
- La nota de regalo admite máximo 140 caracteres.

Con un constructor largo es fácil invertir dos `String`; con setters, el pedido existe incompleto mientras se arma y nadie garantiza las reglas. `Pedido.Builder` concentra todas las validaciones en `build()`, por lo que nunca se entrega un pedido inválido.

#### Participantes GoF

| Participante | Clase | Responsabilidad |
|---|---|---|
| Builder | `Pedido.Builder` | Recibe los datos obligatorios en su constructor, ofrece un método por cada opción (interfaz fluida) y valida en `build()`. |
| Product | `Pedido` | Objeto inmutable: atributos `final`, constructor privado y sin setters. |
| Director | `PedidoService.crear()` | Traduce el `PedidoRequest` recibido a llamadas al builder. |

`ItemPedido` es un `record` (sku, nombre, cantidad y precio unitario). El precio se toma del catálogo, no del cliente. El cupón de prueba `BOSQUE10` aplica 10 % de descuento sobre el subtotal.

#### Endpoint que lo evidencia

```
POST /api/pedidos
GET  /api/pedidos/{id}
```

Ejemplo de petición:

```json
{
  "clienteId": "CLI-001",
  "documentoCliente": "1020304050",
  "pais": "CO",
  "contacto": "ana.perez@correo.com",
  "items": [
    { "sku": "SKU-001", "cantidad": 1 },
    { "sku": "SKU-002", "cantidad": 2 }
  ],
  "direccionEnvio": "Calle 134 # 9-51, Bogotá",
  "cupon": "BOSQUE10",
  "envioExpress": true,
  "canalNotificacion": "EMAIL"
}
```

Respuestas relevantes:

| Caso | Respuesta |
|---|---|
| Pedido válido | `201 Created` con el pedido y el descuento del cupón |
| Pedido sin ítems | `400` con mensaje claro |
| Domicilio sin dirección | `400` con mensaje claro |
| Recoger en tienda y express a la vez | `400` con mensaje claro |
| Nota de regalo de más de 140 caracteres | `400` |
| SKU inexistente | `404` |

Prueba unitaria: `PedidoBuilderTest` (`src/test/java/.../pedidos/`).

---

### 3.2 Abstract Factory

#### Problema que resuelve

Cada país exige una **familia** de piezas que deben ir juntas:

- Calculadora de impuestos.
- Validador de identificación del cliente.
- Generador del comprobante electrónico.

Sin el patrón, `ServicioFacturacion` tendría un `if (pais.equals("CO"))` repetido, sería fácil mezclar el impuesto de un país con el comprobante de otro, y abrir Perú obligaría a editar cada `if`. Con Abstract Factory el servicio recibe `List<FabricaFacturacion>`, arma un mapa por país y factura sin conocer ninguna clase concreta.

| País | Impuesto | Documento del cliente | Comprobante |
|---|---|---|---|
| Colombia (`CO`) | IVA 19 % | Cédula o NIT (6 a 10 dígitos) | Factura electrónica (DIAN) |
| México (`MX`) | IVA 16 % | RFC | CFDI (SAT) |
| Perú (`PE`) | IGV 18 % | RUC de 11 dígitos (el validador también acepta 8 dígitos) | Factura electrónica (SUNAT) |

> Las tarifas y formatos de documento son valores de referencia para el ejercicio, no asesoría tributaria.

#### Participantes GoF

| Participante | Clases |
|---|---|
| Abstract Factory | `FabricaFacturacion` (`pais()`, `crearCalculadora()`, `crearValidador()`, `crearGenerador()`) |
| Concrete Factory | `FabricaColombia`, `FabricaMexico`, `FabricaPeru` (todas `@Component`) |
| Abstract Products | `CalculadoraImpuestos`, `ValidadorIdentificacion`, `GeneradorComprobante` (y el `record Comprobante`) |
| Concrete Products | Colombia: `CalculadoraColombia`, `ValidadorColombia`, `GeneradorDIAN`<br>México: `CalculadoraMexico`, `ValidadorMexico`, `GeneradorCFDI`<br>Perú: `CalculadoraPeru`, `ValidadorPeru`, `GeneradorSUNAT` |
| Client | `ServicioFacturacion` |

#### Endpoint que lo evidencia

El comprobante se incluye en la respuesta de:

```
POST /api/pedidos
```

El país se indica con su código en el campo `pais` (`CO`, `MX` o `PE`).

| Pedido | Resultado |
|---|---|
| `"pais": "CO"` con cédula `1020304050` | Factura electrónica (DIAN), IVA 19 % |
| `"pais": "MX"` con RFC `GODE561231GR8` | CFDI (SAT), IVA 16 % |
| `"pais": "MX"` con la cédula colombiana `1020304050` | `400` (documento inválido para MX) |
| `"pais": "PE"` con RUC de 11 dígitos | Factura electrónica (SUNAT), IGV 18 % |

Prueba unitaria: `ServicioFacturacionTest` (`src/test/java/.../facturacion/`).

---

### 3.3 Adapter

#### Problema que resuelve
 
ShopFlow cobra con dos proveedores cuyos SDK (paquete `externos`) no se pueden modificar y hablan "idiomas" distintos:
 
| | LegacyPay (`LegacyPayClient`) | QuickPay (`QuickPaySdk`) | ShopFlow (`PasarelaDePago`) |
|---|---|---|---|
| Monto | `long` en centavos | `double` en dólares | `BigDecimal` en dólares |
| Respuesta | Texto `"código\|autorización"` (`00` aprobado, `51` fondos insuficientes, otro = no permitida) | Enum `QuickPayStatus` (`OK`, `REJECTED`, `ERROR`) | `ResultadoPago` |
| Fallo | `LegacyPayTimeoutException` | — | `PasarelaNoDisponibleException` |
 
Sin el patrón, `PedidoService` tendría que multiplicar por 100, partir el texto por `|`, revisar el enum y capturar la excepción del SDK. Cambiar de proveedor obligaría a reescribir la lógica de negocio. Con Adapter, cada proveedor tiene una clase que traduce su SDK a `PasarelaDePago`, y **ninguna clase fuera de los adapters importa algo del paquete `externos`**.
 
#### Participantes GoF
 
| Participante | Clase | Responsabilidad |
|---|---|---|
| Target | `PasarelaDePago` | Interfaz propia de ShopFlow: `cobrar(BigDecimal montoUsd, String referencia)`. |
| Adapter | `LegacyPayAdapter` | Guarda un `LegacyPayClient` (composición), convierte dólares a centavos, interpreta `"código\|autorización"` y traduce `LegacyPayTimeoutException` a `PasarelaNoDisponibleException`. |
| Adapter | `QuickPayAdapter` | Guarda un `QuickPaySdk`, convierte `BigDecimal` a `double` y traduce `OK`, `REJECTED` y `ERROR` a un `ResultadoPago`. |
| Adaptee | `LegacyPayClient`, `QuickPaySdk` | SDK de terceros (no se modifican). |
| Client | `PedidoService` | Cobra a través de `PasarelaDePago` sin saber qué proveedor hay detrás. |
 
Es un **adaptador de objeto** (usa composición, no herencia), la variante recomendada por GoF y Head First.
 
#### Diagrama de clases
 

#### Endpoint que lo evidencia
 
```
POST /api/pedidos/{id}/pago
```
 
Cuerpo: `{"proveedor": "LEGACYPAY"}` o `{"proveedor": "QUICKPAY"}`.
 
| Caso | Respuesta |
|---|---|
| Pago aprobado | `200` con el pedido en estado `PAGADO` y su autorización |
| Pedido ya pagado | `409` |
| Más de 2.000 USD con LegacyPay | `409` «Fondos insuficientes» |
| El mismo pedido con QuickPay (hasta 5.000 USD) | `200` |
| Proveedor que no existe | `400` |
| LegacyPay caído con los reintentos desactivados | `503` (no un `500` con la excepción del SDK) |
 
El mismo pedido se paga con cualquiera de los dos proveedores sin cambiar `PedidoService`: solo cambia la pasarela que se toma del mapa por proveedor.
 
Prueba unitaria: `LegacyPayAdapterTest` (`src/test/java/.../pagos/`). Con `new LegacyPayClient(1.0)` (siempre falla) verifica que el adapter lanza `PasarelaNoDisponibleException` y no la excepción del SDK.
 
---
 

### 3.4 Decorator

#### Problema que resuelve
 
LegacyPay falla con timeout el 30 % de las veces (`shopflow.legacypay.probabilidad-timeout=0.3`), así que conviene reintentar. Auditoría exige registrar cada cobro de los dos proveedores (proveedor, monto, resultado y duración). En pruebas, los reintentos deben poder apagarse.
 
Con herencia harían falta seis subclases (`LegacyPayConReintento`, `LegacyPayConAuditoria`, `LegacyPayConReintentoYAuditoria` y las tres de QuickPay), y cada capa nueva duplicaría la cuenta. Con Decorator, cada capa es una clase que implementa `PasarelaDePago` y envuelve otra `PasarelaDePago`. Las capas se combinan en tiempo de ejecución según las propiedades.
 
#### Participantes GoF
 
| Participante | Clase | Responsabilidad |
|---|---|---|
| Component | `PasarelaDePago` | Interfaz común de lo que se envuelve y de los envoltorios. |
| Concrete Component | `LegacyPayAdapter`, `QuickPayAdapter` | El cobro real (los adapters de la ronda 1). |
| Decorator | `PasarelaDecorator` | Clase abstracta: implementa `PasarelaDePago`, tiene una `PasarelaDePago` envuelta y delega en ella por defecto. |
| Concrete Decorator | `ReintentoPago` | Reintenta hasta `max-intentos` veces cuando recibe `PasarelaNoDisponibleException`. |
| Concrete Decorator | `AuditoriaPago` | Mide la duración, delega y registra el resultado en `RegistroAuditoria`. |
| Concrete Decorator | `MetricasPago` | Reto B: cuenta cobros y aprobados y escribe la tasa de aprobación en consola. |
| Ensamblador | `ConfiguracionPagos` | Único lugar que conoce las clases concretas. Construye cada adapter, lo envuelve en capas según las propiedades y entrega las pasarelas indexadas por proveedor. |
 
#### Orden de las capas
 
```
MetricasPago( AuditoriaPago( ReintentoPago( LegacyPayAdapter ) ) )
```
 
La auditoría va **por fuera** del reintento, así que registra exactamente una entrada por cobro, aunque por dentro haya habido varios intentos. Las capas se activan con:
 
```properties
shopflow.pagos.reintentos=true
shopflow.pagos.max-intentos=3
shopflow.pagos.auditoria=true
```
 
Cambiar estas propiedades cambia las capas sin modificar ninguna clase.
 
#### Diagrama de clases
 

La clave está en la doble relación de `PasarelaDecorator` con `PasarelaDePago`: **la implementa** para tener el mismo tipo y **la contiene** para agregar comportamiento. Por eso un decorador puede envolver a otro sin límite y `PedidoService` no distingue una pasarela simple de una envuelta tres veces.
 
#### Endpoint que lo evidencia
 
```
GET  /api/pagos/auditoria
POST /api/pedidos/{id}/pago
```
 
Cada entrada de la auditoría tiene proveedor, referencia, monto, resultado, mensaje y duración en milisegundos. Con los reintentos activos, la gran mayoría de los pagos con LegacyPay se aprueba a pesar de los timeouts, y hay una sola entrada de auditoría por cada cobro.
 
Prueba unitaria: `ReintentoPagoTest` (`src/test/java/.../pagos/`). Una pasarela falsa falla dos veces y aprueba la tercera; la prueba verifica que el pago se aprueba y que hubo exactamente tres llamadas.
 
---

### 3.5 Proxy

_Pendiente (Rol C)._

### 3.6 Factory Method

_Pendiente (Rol C)._

---

## 4. Retos de extensión

### Reto A · Perú (Abstract Factory)

El reto consistió en agregar soporte para Perú **sin modificar ninguna clase existente**. Se agregó una familia nueva en `facturacion/peru/`:

```
facturacion/peru/
├── CalculadoraPeru.java
├── FabricaPeru.java
├── GeneradorSUNAT.java
└── ValidadorPeru.java
```

La extensión agrega:

- Cálculo de IGV del 18 %.
- Validación del documento peruano (RUC de 11 dígitos).
- Comprobante electrónico SUNAT.

`FabricaPeru` está anotada con `@Component`, por lo que Spring la inyecta en `List<FabricaFacturacion>` y `ServicioFacturacion` la incorpora a su mapa por país sin cambios.

**Evidencia:** salida de `git diff --stat` del commit del reto (muestra solo archivos nuevos):

```
mgonz@Compi MINGW64 /d/WorkShopflow/shopflow-base/shopflow-base (main)
$ git diff --stat HEAD~1
 .../shopflow/facturacion/peru/CalculadoraPeru.java | 19 ++++++++++
 .../shopflow/facturacion/peru/FabricaPeru.java     | 41 ++++++++++++++++++++++
 .../shopflow/facturacion/peru/GeneradorSUNAT.java  | 30 ++++++++++++++++
 .../shopflow/facturacion/peru/ValidadorPeru.java   | 21 +++++++++++
 4 files changed, 111 insertions(+)
```

También se guarda en `evidencias/reto-a-peru.txt`.

### Reto B · Métricas (Decorator)

El reto consistió en agregar una capa nueva que mide la tasa de aprobación de cada pasarela **sin modificar ninguna clase existente**, salvo `ConfiguracionPagos`, que es el punto de composición. Se agregó:
 
```
pagos/decoradores/
└── MetricasPago.java      (nuevo)
pagos/ConfiguracionPagos.java   (modificado: una línea para agregar la capa)
```
 
`MetricasPago` extiende `PasarelaDecorator`. Cuenta los cobros y los aprobados de su pasarela con `AtomicInteger` y, después de cada cobro, escribe en consola la tasa de aprobación:
 
```
[Métricas] LEGACYPAY · cobros: 3 · aprobados: 2 · tasa de aprobación: 66,7 %
```
 
En `ConfiguracionPagos` basta con envolver la pasarela ya armada:
 
```java
pasarela = new MetricasPago(pasarela, proveedor);
```
 
`PedidoService`, los adapters y los otros decoradores no cambiaron.
 
**Evidencia:** salida de `git diff --stat` del commit del reto (un archivo nuevo y el único modificado permitido):
 
```
Pendiente: pegar la salida de git diff --stat HEAD~1
```
 
También se guarda en `evidencias/reto-b-metricas.txt`.

### Reto C · WhatsApp (Factory Method)

_Pendiente (Rol C)._

---

## 5. Preguntas de reflexión

**1. Escriba cómo habría quedado `PedidoService` sin Factory Method ni Abstract Factory: ¿cuántos `if` tendría y qué clases habría que modificar para abrir Perú y agregar WhatsApp?**

_Respuesta pendiente._

**2. ¿Por qué las notificaciones usan Factory Method y la facturación usa Abstract Factory? Responda con la diferencia entre crear un producto y crear una familia de productos.**

_Respuesta pendiente._

**3. Invierta el orden de los decoradores (reintento por fuera de la auditoría) y pague cinco pedidos con LegacyPay. ¿Qué cambia en `GET /api/pagos/auditoria`? ¿Qué orden conviene y por qué?**

Con el orden por defecto, `AuditoriaPago(ReintentoPago(LegacyPayAdapter))`, la auditoría ve el cobro completo: hay **una entrada por cobro** y la duración incluye los reintentos.
 
Al invertirlo, `ReintentoPago(AuditoriaPago(LegacyPayAdapter))`, la auditoría queda pegada al adapter y registra **cada intento**. Los timeouts aparecen como entradas fallidas y luego aparece la del intento que sí respondió. Con cinco pagos y un 30 % de timeout se esperan alrededor de siete entradas en lugar de cinco. En nuestra prueba obtuvimos _(pendiente: número observado)_ entradas para 5 pagos.
 
Conviene la **auditoría por fuera**. Auditoría exige registrar *cobros*, que son la operación de negocio, no los intentos técnicos. Además, el criterio de aceptación pide exactamente una entrada por cobro. Si se quisiera medir la inestabilidad del proveedor, eso le corresponde a otra capa (por ejemplo, métricas por intento), no a la auditoría. El ejemplo muestra que en Decorator el orden de las capas cambia el comportamiento.

**4. `CatalogoCacheProxy` y `AuditoriaPago` tienen casi la misma estructura: implementan la interfaz y envuelven un objeto. ¿Por qué uno es Proxy y el otro Decorator?**

_Respuesta pendiente._

**5. Si otro sistema cambia un precio directamente en el catálogo remoto, ¿qué problema tiene la caché del proxy? Proponga una solución.**

_Respuesta pendiente._

**6. `QuickPayAdapter` convierte `BigDecimal` a `double`. ¿Qué riesgo trae y por qué conviene que esa decisión viva en el adapter y no en el dominio?**

`double` es binario de punto flotante y no representa con exactitud muchas fracciones decimales (por ejemplo, `0.1 + 0.2` da `0.30000000000000004`). Con dinero esto produce errores de centavos al sumar, comparar contra límites (como los 5.000 USD de QuickPay) o redondear. Para reducir el riesgo, el adapter redondea a dos decimales antes de convertir (`setScale(2, RoundingMode.HALF_UP).doubleValue()`).
 
Conviene que la conversión viva en el adapter porque usar `double` es una **exigencia del SDK de QuickPay**, no una regla de ShopFlow. Así, el dominio sigue trabajando con `BigDecimal` en todas sus cuentas (subtotal, descuento, impuestos, total), y la pérdida de precisión queda en un solo punto, en la frontera con el código ajeno, donde se puede probar. Si QuickPay cambia su API o llega otro proveedor, solo cambia el adapter (SRP y DIP).

**7. ¿En qué parte del proyecto no convenía aplicar un patrón? Justifique con el criterio «cuándo no usarlo» del Anexo A.**

_Respuesta pendiente._

**8. Para cada patrón, indique el principio SOLID que aplica y cite la clase o la línea de código que lo evidencia.**

 
| Patrón | Principio | Evidencia en el código |
|---|---|---|
| Builder | _Pendiente (Rol A)_ | |
| Abstract Factory | _Pendiente (Rol A)_ | |
| Adapter | DIP | `PedidoService` recibe `Map<String, PasarelaDePago>` y no importa nada del paquete `externos`. |
| Adapter | SRP | La conversión de centavos y del texto `"código\|autorización"` está solo en `LegacyPayAdapter.cobrar(...)`. |
| Decorator | OCP | `MetricasPago` (reto B) se agregó como clase nueva; solo cambió `ConfiguracionPagos`. |
| Decorator | SRP | `ReintentoPago` solo reintenta, `AuditoriaPago` solo registra y `MetricasPago` solo mide. |
| Decorator | LSP | `ConfiguracionPagos` entrega objetos decorados como `PasarelaDePago` y `PedidoService` los usa igual que un adapter sin envolver. |
| Proxy | _Pendiente (Rol C)_ | |
| Factory Method | _Pendiente (Rol C)_ | |
