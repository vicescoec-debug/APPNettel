# Implementación de GPSService.asmx en Spot2

## 1. Objetivo

Implementar en `spot2.nettelcorp.com` una versión HTTPS del servicio SOAP `GPSService.asmx`, compatible con la aplicación Android Nettel Marítimo.

Dirección esperada:

```text
https://spot2.nettelcorp.com/wsNettelMaritimoGPS/GPSService.asmx
```

WSDL esperado:

```text
https://spot2.nettelcorp.com/wsNettelMaritimoGPS/GPSService.asmx?WSDL
```

## 2. Namespace recomendado

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/
```

Todas las operaciones, respuestas, tipos XML y SOAPActions deben utilizar el mismo namespace.

Ejemplo:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/iniciaSesion
```

Cuando el servicio esté publicado, se deben actualizar conjuntamente en Android:

```java
SOAP_ACTION_BASE
WSDL_TARGET_NAMESPACE
SOAP_ADDRESS
```

## 3. Archivo GPSService.asmx

```aspx
<%@ WebService Language="C#"
    CodeBehind="GPSService.asmx.cs"
    Class="Nettel.Maritimo.GPS.GPSService" %>
```

## 4. Clase principal

```csharp
using System;
using System.Collections.Generic;
using System.Web.Services;
using System.Web.Services.Protocols;

namespace Nettel.Maritimo.GPS
{
    [WebService(
        Namespace = "https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/"
    )]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    public class GPSService : WebService
    {
        // Implementar las ocho operaciones obligatorias.
    }
}
```

## 5. Resultado común

```csharp
[Serializable]
public class Result<T>
{
    public int ActionResult { get; set; }
    public string MessageResult { get; set; }
    public string TokenResult { get; set; }
    public T ObjectResult { get; set; }
    public T ObjectResult2 { get; set; }
}
```

Para operaciones sin datos:

```csharp
[Serializable]
public class Result
{
    public int ActionResult { get; set; }
    public string MessageResult { get; set; }
    public string TokenResult { get; set; }
    public object ObjectResult { get; set; }
    public object ObjectResult2 { get; set; }
}
```

Valores permitidos:

| Valor | Resultado |
|---:|---|
| `0` | Successful |
| `1` | Warning |
| `2` | Error |

Los siguientes nombres deben conservarse exactamente:

```text
ActionResult
MessageResult
TokenResult
ObjectResult
ObjectResult2
```

## 6. Operaciones obligatorias

### 6.1 iniciaSesion

```csharp
[WebMethod]
public Result iniciaSesion(string usuario, string clave, string id)
```

Debe:

1. Validar campos obligatorios.
2. Buscar el usuario mediante una consulta parametrizada.
3. Verificar que la cuenta esté activa.
4. Comparar la contraseña con un hash seguro.
5. Validar o registrar el identificador Android `id`.
6. Crear una sesión con vencimiento.
7. Generar un token criptográficamente aleatorio.
8. Devolver `ActionResult = 0` y el token en `TokenResult`.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/iniciaSesion
```

Respuesta de éxito:

```xml
<iniciaSesionResult>
  <ActionResult>0</ActionResult>
  <MessageResult>Inicio de sesión correcto</MessageResult>
  <TokenResult>token-seguro</TokenResult>
</iniciaSesionResult>
```

### 6.2 cierraSesion

```csharp
[WebMethod]
public Result cierraSesion(string id)
```

Debe invalidar la sesión correspondiente al identificador o token recibido.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/cierraSesion
```

### 6.3 consultaRol

```csharp
[WebMethod]
public Result consultaRol(string id)
```

La aplicación actual espera `ObjectResult` como una cadena:

```text
1,Administrador
```

El valor anterior contiene:

```text
IdRol,DescripcionRol
```

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/consultaRol
```

### 6.4 consultaUsuarios

```csharp
[WebMethod]
public Result<List<Usuario>> consultaUsuarios(string token)
```

Debe:

1. Validar el token.
2. Identificar el usuario y su rol.
3. Limitar los resultados a los clientes autorizados.
4. No devolver contraseñas ni hashes.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/consultaUsuarios
```

### 6.5 consultaDispositivos

```csharp
[WebMethod]
public Result<List<Dispositivo>> consultaDispositivos(
    string token,
    string usuario
)
```

Debe devolver:

- `ObjectResult`: dispositivos con su última posición.
- `ObjectResult2`: dispositivos resumidos del cliente.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/consultaDispositivos
```

### 6.6 consultaDispositivoHistorico

```csharp
[WebMethod]
public Result<List<Dispositivo>> consultaDispositivoHistorico(
    string token,
    string usuario,
    string serie,
    string fechaDesde,
    string fechaHasta,
    string numReg
)
```

Debe:

1. Validar el token.
2. Confirmar que el usuario puede consultar la serie.
3. Validar las fechas.
4. Impedir rangos excesivos.
5. Convertir `numReg` a entero con un máximo configurado.
6. Ordenar los registros de forma consistente.
7. Utilizar consultas SQL parametrizadas.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/consultaDispositivoHistorico
```

### 6.7 consultaAlarmas

```csharp
[WebMethod]
public Result<List<Alarma>> consultaAlarmas(
    string token,
    int ultimaAlarma
)
```

Debe devolver únicamente alarmas posteriores a `ultimaAlarma` y pertenecientes a clientes autorizados para el token.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/consultaAlarmas
```

### 6.8 registraAlarmasLeidas

```csharp
[WebMethod]
public Result registraAlarmasLeidas(
    string token,
    string alarmasLeidas
)
```

La aplicación envía los IDs separados por comas:

```text
15,16,20
```

El servidor debe:

1. Validar el token.
2. Separar los IDs.
3. Confirmar que cada valor sea entero.
4. Confirmar que cada alarma pertenezca al usuario.
5. Actualizar mediante parámetros, nunca concatenando SQL.
6. Ejecutar la operación dentro de una transacción.

SOAPAction:

```text
https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/registraAlarmasLeidas
```

## 7. Entidad Usuario

```csharp
[Serializable]
public class Usuario
{
    public string IdUsuario { get; set; }
    public string NombreCompleto { get; set; }
    public string TipoIdentificacion { get; set; }
    public string Identificacion { get; set; }
    public string Direccion { get; set; }
    public string Telefono { get; set; }
    public string Ciudad { get; set; }
    public string EmailPrincipal { get; set; }
    public string EmailAlterno1 { get; set; }
    public string EmailAlterno2 { get; set; }
    public string EmailAlterno3 { get; set; }
    public string EmailAlterno4 { get; set; }
}
```

La colección debe aparecer en XML como:

```text
ArrayOfUsuario/Usuario
```

## 8. Entidad Dispositivo

```csharp
[Serializable]
public class Dispositivo
{
    public int IdDispositivo { get; set; }
    public string Serie { get; set; }
    public long IdPosicion { get; set; }
    public decimal Latitud { get; set; }
    public string LatitudGMS { get; set; }
    public decimal Longitud { get; set; }
    public string LongitudGMS { get; set; }
    public string TipoPosicion { get; set; }
    public int Rumbo { get; set; }
    public decimal Velocidad { get; set; }
    public DateTime Ubicado { get; set; }
    public string Estado { get; set; }
    public string Nombre { get; set; }
    public string Matricula { get; set; }
    public string Detalle { get; set; }
    public int IdUltimaPosicion { get; set; }
    public string SatamaticsUser { get; set; }
    public int TieneAlerta { get; set; }
    public int AlertaRobo { get; set; }
}
```

Reglas:

- `TieneAlerta`: `0` o `1`.
- `AlertaRobo`: `0` o `1`.
- `Ubicado`: fecha ISO 8601 válida.
- `Latitud` y `Longitud`: valores numéricos, no cadenas vacías.

La colección debe aparecer como:

```text
ArrayOfDispositivo/Dispositivo
```

## 9. Entidad Alarma

```csharp
[Serializable]
public class Alarma
{
    public int id_alarma { get; set; }
    public int id_posicion { get; set; }
    public string tipo_alarma { get; set; }
    public string mensaje { get; set; }
    public int id_dispositivo { get; set; }
    public int id_geocerca { get; set; }
    public double latitud { get; set; }
    public double longitud { get; set; }
    public DateTime ubicado { get; set; }
    public string serie { get; set; }
    public string nombre { get; set; }
    public string nave { get; set; }
    public string matricula { get; set; }
    public string cliente { get; set; }
    public string emailPrincipal { get; set; }
    public string emailAlterno1 { get; set; }
    public string emailAlterno2 { get; set; }
    public string emailAlterno3 { get; set; }
    public string emailAlterno4 { get; set; }
    public string asunto { get; set; }
}
```

Los nombres de esta entidad son sensibles a mayúsculas y minúsculas. La aplicación espera nombres en minúsculas como `id_alarma`, `tipo_alarma` y `ubicado`.

La colección debe aparecer como:

```text
ArrayOfAlarma/Alarma
```

## 10. Ejemplo SOAP 1.1

Solicitud:

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <iniciaSesion xmlns="https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/">
      <usuario>usuario</usuario>
      <clave>clave</clave>
      <id>android-id</id>
    </iniciaSesion>
  </soap:Body>
</soap:Envelope>
```

Cabeceras:

```http
Content-Type: text/xml; charset=utf-8
SOAPAction: "https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/iniciaSesion"
```

Respuesta:

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <iniciaSesionResponse xmlns="https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/">
      <iniciaSesionResult>
        <ActionResult>0</ActionResult>
        <MessageResult>Inicio de sesión correcto</MessageResult>
        <TokenResult>token-seguro</TokenResult>
      </iniciaSesionResult>
    </iniciaSesionResponse>
  </soap:Body>
</soap:Envelope>
```

## 11. Seguridad obligatoria

### Transporte

- HTTPS obligatorio.
- TLS 1.2 o superior.
- Certificado público válido para `spot2.nettelcorp.com`.
- Redirección de HTTP a HTTPS.
- Deshabilitar protocolos y cifrados obsoletos.

### Contraseñas

- Nunca almacenar contraseñas en texto plano.
- Utilizar Argon2id, bcrypt o PBKDF2 con salt único.
- Nunca devolver contraseñas o hashes mediante SOAP.
- Nunca escribir contraseñas en logs.

### Tokens

- Generación criptográficamente aleatoria.
- Vencimiento configurable.
- Invalidación al cerrar sesión.
- Renovación controlada.
- Asociación con usuario y dispositivo.
- Almacenamiento seguro en la base de datos.

### Base de datos

- Consultas parametrizadas.
- Usuario de base de datos con permisos mínimos.
- Cadenas de conexión fuera del código fuente.
- Transacciones para operaciones múltiples.
- Límites de tiempo y cantidad de registros.

### Autorización

Cada consulta debe validar:

```text
Token -> Usuario -> Rol -> Cliente -> Dispositivo/Alarma
```

No basta con validar únicamente que el token exista.

## 12. Manejo de errores

Los errores de negocio deben conservar la estructura `Result`:

```xml
<ActionResult>1</ActionResult>
<MessageResult>La sesión ha expirado</MessageResult>
```

Los errores internos deben devolver:

```xml
<ActionResult>2</ActionResult>
<MessageResult>No fue posible completar la operación</MessageResult>
```

No se deben exponer:

- Consultas SQL.
- Cadenas de conexión.
- Rutas físicas.
- Stack traces.
- Contraseñas.
- Tokens completos en logs.

## 13. Configuración IIS

Se requiere:

1. IIS con ASP.NET habilitado.
2. Application Pool compatible con la versión de .NET utilizada.
3. Aplicación o directorio virtual `wsNettelMaritimoGPS`.
4. Binding HTTPS para `spot2.nettelcorp.com`.
5. Certificado TLS instalado y asociado al puerto 443.
6. Permisos de lectura y ejecución para la identidad del Application Pool.
7. Cadena de conexión protegida en `web.config`.
8. Página `GPSService.asmx?WSDL` accesible por HTTPS.
9. Métodos HTTP innecesarios bloqueados.
10. Logs y monitoreo habilitados.

## 14. Configuración web.config orientativa

```xml
<?xml version="1.0"?>
<configuration>
  <connectionStrings>
    <add
      name="NettelGps"
      connectionString="PROTEGER_FUERA_DEL_REPOSITORIO"
      providerName="System.Data.SqlClient" />
  </connectionStrings>

  <system.web>
    <compilation debug="false" targetFramework="4.8" />
    <httpRuntime targetFramework="4.8" executionTimeout="60" />
    <customErrors mode="On" />
  </system.web>

  <system.webServer>
    <httpProtocol>
      <customHeaders>
        <add name="X-Content-Type-Options" value="nosniff" />
        <add name="X-Frame-Options" value="DENY" />
        <add name="Strict-Transport-Security"
             value="max-age=31536000; includeSubDomains" />
      </customHeaders>
    </httpProtocol>
  </system.webServer>
</configuration>
```

La cadena de conexión real no debe almacenarse en este documento ni publicarse en el repositorio.

## 15. Dependencias de datos

El servicio debe tener acceso funcional a información equivalente a:

- Usuarios.
- Contraseñas o hashes.
- Roles.
- Sesiones y tokens.
- Clientes.
- Relación usuario-cliente.
- Dispositivos y naves.
- Posiciones actuales.
- Histórico de posiciones.
- Alarmas.
- Estado leído de alarmas.

Si el servicio Spot2 utiliza una base diferente, deberá replicar o sincronizar esos datos antes de cambiar la aplicación.

## 16. Pruebas obligatorias

### Pruebas de contrato

- WSDL disponible por HTTPS.
- Las ocho operaciones aparecen en el WSDL.
- Los SOAPActions coinciden exactamente.
- Los parámetros respetan nombre, orden y tipo.
- Las respuestas conservan `Result`.
- Las colecciones se serializan con la estructura esperada.

### Pruebas funcionales

- Login correcto.
- Login incorrecto.
- Usuario bloqueado o inactivo.
- Cierre de sesión.
- Consulta de rol.
- Consulta de usuarios autorizados.
- Consulta de dispositivos.
- Consulta histórica con diferentes rangos.
- Consulta de alertas nuevas.
- Registro de alertas leídas.
- Token inválido y token vencido.

### Pruebas de seguridad

- No acepta HTTP sin redirección.
- No devuelve contraseñas.
- No permite consultar dispositivos de otro cliente.
- No permite marcar alarmas ajenas.
- Resiste parámetros SQL maliciosos.
- Limita consultas históricas excesivas.
- Registra errores sin datos sensibles.

## 17. Criterios de aceptación

El servicio se considerará listo cuando:

1. `GPSService.asmx?WSDL` responda HTTP 200 por HTTPS.
2. Las ocho operaciones estén publicadas.
3. El namespace y los SOAPActions sean consistentes.
4. La aplicación pueda iniciar y cerrar sesión.
5. Se carguen roles, usuarios y dispositivos.
6. Se visualicen posiciones actuales e históricas.
7. Se reciban y registren alertas.
8. No exista tráfico SOAP por HTTP.
9. Las contraseñas y tokens no aparezcan en logs.
10. La aplicación compile y pase las pruebas con la nueva dirección.

## 18. Cambio Android posterior al despliegue

Después de publicar y validar el servicio, actualizar:

```java
private static final String SOAP_ACTION_BASE =
    "https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/";

public static final String WSDL_TARGET_NAMESPACE =
    "https://spot2.nettelcorp.com/Nettel/Maritimo/GPS/";

public static final String SOAP_ADDRESS =
    "https://spot2.nettelcorp.com/wsNettelMaritimoGPS/GPSService.asmx";
```

Finalmente, eliminar de `network_security_config.xml` la excepción HTTP para `www.movilcomsa.com`.
