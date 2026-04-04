package co.edu.uceva.buildcheck.modules.materiales.DTO;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialStockBajoDTO {
    private Long id;
    private String nombre;
    private Integer stockActual;
    private Integer stockReferencia;
    private String unidadMedida;
    private String mensaje;

    public MaterialStockBajoDTO(Long id, String nombre, Integer stockActual, Integer stockReferencia,
            String unidadMedida) {
        this.id = id;
        this.nombre = nombre;
        this.stockActual = stockActual;
        this.stockReferencia = stockReferencia;
        this.unidadMedida = unidadMedida;

        if (stockReferencia > 0) {
            double porcentaje = (stockActual * 100.0) / stockReferencia;
            this.mensaje = String.format("Nivel crítico: %.1f%%", porcentaje);
        } else {
            this.mensaje = "Sin referencia de stock";
        }
    }
}