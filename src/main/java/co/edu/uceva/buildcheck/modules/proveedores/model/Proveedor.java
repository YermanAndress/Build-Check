package co.edu.uceva.buildcheck.modules.proveedores.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creador", nullable = false, length = 100)
    private String usuarioCreador;

    @PrePersist
    public void prePersist(){
        this.fechaCreacion = LocalDateTime.now();
        if (this.usuarioCreador == null) {
            this.usuarioCreador = "system";
        }
    }
}
