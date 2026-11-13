package ferrefix.catalogo.dto;

import jakarta.validation.constraints.*;

public record OfertaRequest(@NotBlank @Size(max=255) String imagen,
            @NotBlank @Size(max=500) @Pattern(regexp="^/(?!/)[a-zA-Z0-9/_?=&%#.-]*$") String enlace,
            @NotBlank @Size(max=200) String alt) {}
