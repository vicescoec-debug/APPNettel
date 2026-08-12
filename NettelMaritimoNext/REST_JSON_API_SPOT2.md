# Contrato REST + JSON para Spot2/Linux

Base URL:

```text
https://spot1.nettelcorp.com/api/v1/
```

Todas las peticiones deben usar:

```http
Content-Type: application/json; charset=utf-8
Accept: application/json
```

Respuesta estÃ¡ndar:

```json
{
  "action": 0,
  "success": true,
  "message": "OK",
  "token": "TOKEN_DE_SESION_OPCIONAL",
  "data": []
}
```

ConvenciÃ³n:

- `action: 0` = operaciÃ³n exitosa.
- `action` distinto de `0` = error funcional.
- `message` o `mensaje` = texto visible para el usuario.
- `data` puede ser arreglo u objeto.
- El token se puede enviar en el cuerpo JSON para compatibilidad con la app actual.

## Endpoints requeridos

### POST `/auth/login`

Entrada:

```json
{
  "usuario": "usuario",
  "clave": "clave",
  "device_id": "android_id"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "message": "OK",
  "token": "session-token"
}
```

### POST `/auth/logout`

Entrada:

```json
{
  "token": "session-token"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "message": "SesiÃ³n cerrada"
}
```

### POST `/auth/role`

Entrada:

```json
{
  "token": "session-token"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "data": {
    "rol": "Administrador"
  }
}
```

### POST `/auth/password/verify-temporary`

Entrada:

```json
{
  "id_usuario": "usuario"
}
```

Salida compatible:

```json
{
  "mensaje": 0,
  "success": true
}
```

Usar `mensaje: 1` cuando el usuario debe cambiar contraseÃ±a.

### POST `/auth/password/forgot`

Entrada:

```json
{
  "customer_id": "identificacion_cliente",
  "username": "usuario"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "message": "Solicitud procesada"
}
```

### POST `/auth/password/change`

Entrada:

```json
{
  "usuario": "usuario",
  "claveactual": "clave_actual",
  "clave": "nueva_clave"
}
```

Salida compatible:

```json
{
  "action": 0,
  "success": true,
  "mensaje": 1,
  "message": "ContraseÃ±a actualizada"
}
```

### POST `/users`

Entrada:

```json
{
  "token": "session-token"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "data": [
    {
      "IdUsuario": "cliente1",
      "NombreCompleto": "Nombre Cliente",
      "Identificacion": "0999999999",
      "Ciudad": "Guayaquil",
      "Telefono": "0999999999",
      "EmailPrincipal": "correo@dominio.com"
    }
  ]
}
```

### POST `/users/update`

Actualiza los datos editables del usuario logoneado.

Entrada:

```json
{
  "token": "session-token",
  "usuario": "usuario",
  "IdUsuario": "usuario",
  "email": "correo@dominio.com",
  "EmailPrincipal": "correo@dominio.com",
  "correo": "correo@dominio.com",
  "telefono": "0999999999",
  "Telefono": "0999999999",
  "celular": "0999999999"
}
```

Salida esperada:

```json
{
  "action": 0,
  "success": true,
  "message": "Información actualizada"
}
```

Compatibilidad: la app también intenta `POST /users/profile/update` si `/users/update` no está disponible.

### POST `/devices`

Entrada:

```json
{
  "token": "session-token",
  "usuario": "usuario"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "data": [
    {
      "IdDispositivo": "1",
      "Serie": "ABC123",
      "Nombre": "Nave 1",
      "Matricula": "MT-001",
      "Estado": "Activo",
      "Ubicado": "2026-06-23 10:30:00",
      "Latitud": "-2.170998",
      "Longitud": "-79.922359",
      "Velocidad": "12.5",
      "TieneAlerta": "0",
      "AlertaRobo": "0"
    }
  ]
}
```

### POST `/devices/history`

Entrada:

```json
{
  "token": "session-token",
  "usuario": "usuario",
  "serie": "ABC123",
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-23",
  "numReg": "500"
}
```

Salida: mismo formato de objetos que `/devices`.

### POST `/alerts`

Entrada:

```json
{
  "token": "session-token",
  "ultimaAlarma": "0"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "data": [
    {
      "id_alarma": "1",
      "tipo_alarma": "SOS",
      "mensaje": "Alerta recibida",
      "ubicado": "2026-06-23 10:30:00",
      "nave": "Nave 1",
      "matricula": "MT-001",
      "cliente": "Cliente",
      "latitud": "-2.170998",
      "longitud": "-79.922359"
    }
  ]
}
```

### POST `/alerts/read`

Entrada:

```json
{
  "token": "session-token",
  "alarmasLeidas": "1,2,3"
}
```

Salida:

```json
{
  "action": 0,
  "success": true,
  "message": "Alertas marcadas como leÃ­das"
}
```

## Recomendaciones para Spot2/Linux

- Implementar con Nginx + PHP-FPM/Laravel, Node.js/Express, Python/FastAPI o similar.
- Mantener HTTPS obligatorio.
- No publicar `.asmx`; no usar SOAP/XML.
- Registrar logs por endpoint, usuario, IP, token y cÃ³digo de respuesta.
- Responder siempre JSON, incluso en errores.
- Usar cÃ³digos HTTP:
  - `200` para operaciÃ³n procesada.
  - `400` para JSON invÃ¡lido.
  - `401` para token invÃ¡lido.
  - `403` para permisos insuficientes.
  - `500` para error interno.

