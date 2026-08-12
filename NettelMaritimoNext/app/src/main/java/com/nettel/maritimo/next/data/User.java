package com.nettel.maritimo.next.data;

import java.util.Map;

public class User {
    public String id, name, identification, city, phone, email;

    public static User from(Map<String, String> m) {
        User u = new User();
        u.id = v(m, "IdUsuario", "id_usuario", "usuario", "username", "id");
        u.name = v(m, "NombreCompleto", "nombre_completo", "nombre", "name", "Nombre");
        u.identification = v(m, "Identificacion", "identificacion", "cedula", "ruc");
        u.city = v(m, "Ciudad", "ciudad", "city");
        u.phone = v(m, "Telefono", "telefono", "celular", "Celular", "phone", "mobile");
        u.email = v(m, "EmailPrincipal", "email_principal", "correo", "Correo", "email", "Email");
        return u;
    }

    @Override
    public String toString() {
        return text(name, "Sin nombre")
                + "\n" + text(id, "Sin usuario") + " · " + text(identification, "S/I") + " · " + text(city, "S/C")
                + "\n" + text(email, "Sin correo") + " · " + text(phone, "Sin celular");
    }

    static String v(Map<String, String> m, String... keys) {
        for (String key : keys) {
            String value = m.get(key);
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private static String text(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
