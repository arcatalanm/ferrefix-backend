package ferrefix.auth.model;

import java.util.Locale;

public final class Rut {
    private Rut() {}

    public static String normalizar(String valor) {
        String limpio = valor == null ? "" : valor.strip().toUpperCase(Locale.ROOT);
        if (!limpio.matches("(?:[1-9][0-9]{6,7}|[1-9][0-9]?\\.[0-9]{3}\\.[0-9]{3})-?[0-9K]"))
            throw new IllegalArgumentException("Ingresa un RUT válido.");
        limpio = limpio.replace(".", "").replace("-", "");
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        char verificador = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        if (limpio.charAt(limpio.length() - 1) != verificador)
            throw new IllegalArgumentException("El dígito verificador del RUT no es válido.");
        return cuerpo + "-" + verificador;
    }
}
