package co.edu.uceva.buildcheck.modules.materiales.DTO;

import co.edu.uceva.buildcheck.modules.factura_material.DTO.FacturaMaterialDTO;
import co.edu.uceva.buildcheck.modules.materiales.model.UnidadMedida;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MaterialDTO {
    Long id;
    String nombre;
    String descripcion;
    UnidadMedida unidadMedida;
    Double precioUnitario;
    Double stockActual;
    LocalDateTime fechaCreacion;
    String usuarioCreador;

    private List<FacturaMaterialDTO> facturas;
}
