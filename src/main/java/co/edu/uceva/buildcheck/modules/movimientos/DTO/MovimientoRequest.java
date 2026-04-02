package co.edu.uceva.buildcheck.modules.movimientos.DTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MovimientoRequest {
    private String tipoMovimiento; // ENTRADA o SALIDA
    private Double cantidad;
    private LocalDate fecha;
    private Long usuarioId;
    private String evidenciaFotografica;
    private Long proyectoId;
    private Long materialId;
}
