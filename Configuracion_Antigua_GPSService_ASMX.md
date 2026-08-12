# Configuración antigua de `GPSService.asmx`

## 1. Identificación

Este documento describe el contrato del servicio SOAP antiguo utilizado por la aplicación Android **Nettel Marítimo GPS**.

### Dirección del servicio

```text
http://www.movilcomsa.com/wsNettelMaritimoGPS/GPSService.asmx
```

### Dirección del WSDL

```text
http://www.movilcomsa.com/wsNettelMaritimoGPS/GPSService.asmx?WSDL
```

### Tecnología

- ASP.NET Web Services (`.asmx`).
- SOAP 1.1 y SOAP 1.2.
- Estilo `document`.
- Cuerpo XML `literal`.
- Transporte HTTP.
- Codificación UTF-8.

## 2. Namespace publicado en el WSDL antiguo

El WSDL consultado publica el siguiente namespace:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/
```

Ejemplo de SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/iniciaSesion
```

> **Advertencia:** el código Android heredado contiene un namespace diferente:

```text
http://www.movilcomsa.com/Nettel/Maritimo/GPS/
```

Al desplegar una nueva versión debe utilizarse un solo namespace en el servidor y en la aplicación. No se deben mezclar ambos valores.

## 3. Definición general del servicio

```csharp
[WebService(
    Namespace = "https://spot.netteltracker.com/Nettel/Maritimo/GPS/"
)]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class GPSService : WebService
{
    // Operaciones descritas en las siguientes secciones.
}
```

Archivo `.asmx` aproximado:

```aspx
<%@ WebService Language="C#"
    CodeBehind="GPSService.asmx.cs"
    Class="Nettel.Maritimo.GPS.GPSService" %>
```

## 4. Resultado común

La mayoría de las operaciones devuelven una estructura `Result`:

```csharp
public class Result
{
    public int ActionResult { get; set; }
    public string MessageResult { get; set; }
    public string TokenResult { get; set; }
    public object ObjectResult { get; set; }
    public object ObjectResult2 { get; set; }
}
```

### Valores de `ActionResult`

| Valor | Significado |
|---:|---|
| `0` | Successful |
| `1` | Warning |
| `2` | Error |

Los nombres XML deben conservar exactamente las mayúsculas y minúsculas:

```text
ActionResult
MessageResult
TokenResult
ObjectResult
ObjectResult2
```

## 5. Operaciones SOAP

### 5.1 `iniciaSesion`

SOAPAction antigua:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/iniciaSesion
```

Firma:

```csharp
Result iniciaSesion(string usuario, string clave, string id)
```

Parámetros:

| Parámetro | Tipo XML | Obligatorio práctico | Descripción |
|---|---|---|---|
| `usuario` | `string` | Sí | Nombre del usuario. |
| `clave` | `string` | Sí | Contraseña. |
| `id` | `string` | Sí | Identificador Android del dispositivo. |

Respuesta XML:

```text
iniciaSesionResponse/iniciaSesionResult
```

### 5.2 `cierraSesion`

```csharp
Result cierraSesion(string id)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/cierraSesion
```

`id` corresponde al identificador utilizado para la sesión.

### 5.3 `consultaRol`

```csharp
Result consultaRol(string id)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/consultaRol
```

La aplicación espera en `ObjectResult` una cadena con este formato:

```text
1,Administrador
```

El primer valor es el identificador numérico del rol y el segundo su descripción.

### 5.4 `consultaAlarmas`

```csharp
ResultOfListOfAlarma consultaAlarmas(string token, int ultimaAlarma)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/consultaAlarmas
```

Parámetros:

| Parámetro | Tipo XML | Descripción |
|---|---|---|
| `token` | `string` | Token o identificador de sesión. |
| `ultimaAlarma` | `int` | Último ID almacenado por la aplicación. |

`ObjectResult` contiene `ArrayOfAlarma`.

### 5.5 `registraAlarmasLeidas`

```csharp
Result registraAlarmasLeidas(string token, string alarmasLeidas)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/registraAlarmasLeidas
```

`alarmasLeidas` contiene identificadores separados por comas:

```text
15,16,20
```

### 5.6 `consultaUsuarios`

```csharp
ResultOfListOfUsuario consultaUsuarios(string token)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/consultaUsuarios
```

`ObjectResult` contiene `ArrayOfUsuario`.

### 5.7 `consultaDispositivos`

```csharp
ResultOfListOfDispositivo consultaDispositivos(
    string token,
    string usuario
)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/consultaDispositivos
```

Respuesta:

- `ObjectResult`: lista de dispositivos y su última posición.
- `ObjectResult2`: lista resumida de dispositivos del cliente.

### 5.8 `consultaDispositivoHistorico`

```csharp
ResultOfListOfDispositivo consultaDispositivoHistorico(
    string token,
    string usuario,
    string serie,
    string fechaDesde,
    string fechaHasta,
    string numReg
)
```

SOAPAction:

```text
https://spot.netteltracker.com/Nettel/Maritimo/GPS/consultaDispositivoHistorico
```

Las fechas se reciben como cadenas y `numReg` indica el límite solicitado.

## 6. Entidad `Alarma`

El WSDL antiguo define los siguientes campos:

| Campo | Tipo XML | Uso en la app |
|---|---|---|
| `id_alarma` | `int` | Sí |
| `id_posicion` | `int` | No directo |
| `tipo_alarma` | `string` | Sí |
| `mensaje` | `string` | Sí |
| `id_dispositivo` | `int` | Sí |
| `id_geocerca` | `int` | No directo |
| `latitud` | `double` | Sí |
| `longitud` | `double` | Sí |
| `ubicado` | `dateTime` | Sí |
| `serie` | `string` | Sí |
| `nombre` | `string` | No directo |
| `nave` | `string` | Sí |
| `matricula` | `string` | Sí |
| `cliente` | `string` | Sí |
| `emailPrincipal` | `string` | No directo |
| `emailAlterno1` | `string` | No directo |
| `emailAlterno2` | `string` | No directo |
| `emailAlterno3` | `string` | No directo |
| `emailAlterno4` | `string` | No directo |
| `asunto` | `string` | No directo |

La colección debe serializarse como:

```text
ArrayOfAlarma/Alarma
```

## 7. Entidad `Usuario`

La aplicación consume estos campos:

```text
IdUsuario
NombreCompleto
TipoIdentificacion
Identificacion
Direccion
Telefono
Ciudad
EmailPrincipal
EmailAlterno1
EmailAlterno2
EmailAlterno3
EmailAlterno4
```

El contrato antiguo también expone:

```text
Clave
Token
UltimoToken
IdDispositivo
```

> La propiedad `Clave` no debería devolverse en una implementación nueva.

La colección se serializa como:

```text
ArrayOfUsuario/Usuario
```

## 8. Entidad `Dispositivo`

| Campo | Tipo XML |
|---|---|
| `IdDispositivo` | `int` |
| `Serie` | `string` |
| `IdPosicion` | `long` |
| `Latitud` | `decimal` |
| `LatitudGMS` | `string` |
| `Longitud` | `decimal` |
| `LongitudGMS` | `string` |
| `TipoPosicion` | `string` |
| `Rumbo` | `int` |
| `Velocidad` | `decimal` |
| `Ubicado` | `dateTime` |
| `Estado` | `string` |
| `Nombre` | `string` |
| `Matricula` | `string` |
| `Detalle` | `string` |
| `IdUltimaPosicion` | `int` |
| `SatamaticsUser` | `string` |
| `TieneAlerta` | `int` (`0` o `1`) |
| `AlertaRobo` | `int` (`0` o `1`) |

La colección se serializa como:

```text
ArrayOfDispositivo/Dispositivo
```

## 9. Clase base `Request`

Las entidades `Usuario` y `Dispositivo` heredan en el WSDL antiguo los siguientes campos:

```text
ciEstado             string
UsuarioIngreso       string
UsuarioModificacion  string
FechaIngreso         dateTime
FechaModificacion    dateTime
CodigoAutorizacion   string
```

## 10. Ejemplo de solicitud SOAP 1.1

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <iniciaSesion xmlns="https://spot.netteltracker.com/Nettel/Maritimo/GPS/">
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
SOAPAction: "https://spot.netteltracker.com/Nettel/Maritimo/GPS/iniciaSesion"
```

## 11. Ejemplo de respuesta SOAP 1.1

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <iniciaSesionResponse xmlns="https://spot.netteltracker.com/Nettel/Maritimo/GPS/">
      <iniciaSesionResult>
        <ActionResult>0</ActionResult>
        <MessageResult>Inicio de sesión correcto</MessageResult>
        <TokenResult>token-de-sesion</TokenResult>
      </iniciaSesionResult>
    </iniciaSesionResponse>
  </soap:Body>
</soap:Envelope>
```

## 12. Binding y direcciones antiguas

El WSDL contiene dos bindings:

```text
GPSServiceSoap
GPSServiceSoap12
```

Ambos apuntan a:

```text
http://www.movilcomsa.com/wsNettelMaritimoGPS/GPSService.asmx
```

## 13. Dependencias que no aparecen en el WSDL

Para funcionar realmente, el servicio requiere además:

- Código C# de cada método web.
- Cadena de conexión a la base de datos.
- Tablas, vistas o procedimientos almacenados de usuarios, roles, dispositivos, posiciones y alarmas.
- Validación de credenciales.
- Administración de sesiones o tokens.
- Reglas de acceso usuario–cliente–dispositivo.
- Registro de alarmas leídas.
- Configuración IIS y ASP.NET.
- Logs del servidor.
- Manejo de errores y transacciones.

## 14. Recomendación para la migración

1. Copiar inicialmente el contrato antiguo sin cambiar nombres ni estructuras.
2. Publicarlo en HTTPS bajo `spot2.nettelcorp.com`.
3. Definir un único namespace nuevo o conservar temporalmente el antiguo.
4. Actualizar conjuntamente `SOAP_ADDRESS`, `WSDL_TARGET_NAMESPACE` y `SOAP_ACTION_BASE` en Android.
5. Ejecutar pruebas de contrato para las ocho operaciones.
6. Solo después retirar la excepción HTTP del archivo `network_security_config.xml`.

## 15. Fuente de esta especificación

- WSDL público del servicio antiguo consultado el 22 de junio de 2026.
- Código fuente Android de Nettel Marítimo GPS.
- Mapeos de `Usuario`, `Dispositivo`, `Alerta` y `Result` utilizados por la aplicación.
