package co.edu.uceva.buildcheck.modules.usuario_proyecto.service;

import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.repository.IUsuarioProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioProyectoServiceImpl implements IUsuarioProyectoService {

    @Autowired
    private IUsuarioProyectoRepository usuarioProyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IProyectoRepository proyectoRepository;

    @Override
    public UsuarioProyecto agregarUsuarioAlProyecto(Long usuarioId, Long proyectoId, RolNombre rol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyectoId));

        // Verificar que el usuario no sea ya miembro
        if (usuarioProyectoRepository.existsByUsuarioAndProyecto(usuario, proyecto)) {
            throw new OperacionNoPermitidaException("El usuario ya es miembro del proyecto");
        }

        UsuarioProyecto usuarioProyecto = new UsuarioProyecto();
        usuarioProyecto.setUsuario(usuario);
        usuarioProyecto.setProyecto(proyecto);
        usuarioProyecto.setRolProyecto(rol != null ? rol : RolNombre.ROLE_VIEWER);

        return usuarioProyectoRepository.save(usuarioProyecto);
    }

    @Override
    public UsuarioProyecto cambiarRolUsuario(Long usuarioId, Long proyectoId, RolNombre nuevoRol) {
        UsuarioProyecto usuarioProyecto = usuarioProyectoRepository
                .findByUsuario_IdAndProyecto_Id(usuarioId, proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no es miembro del proyecto"));

        usuarioProyecto.setRolProyecto(nuevoRol);
        return usuarioProyectoRepository.save(usuarioProyecto);
    }

    @Override
    public void removerUsuarioDelProyecto(Long usuarioId, Long proyectoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));

        usuarioProyectoRepository.deleteByUsuarioAndProyecto(usuario, proyecto);
    }

    @Override
    public List<UsuarioProyecto> obtenerProyectosDelUsuario(Long usuarioId) {
        // Validar que el usuario existe
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        // Usar query con JOIN FETCH para evitar LAZY loading
        return usuarioProyectoRepository.findByUsuarioIdWithProyectoEager(usuarioId);
    }

    @Override
    public List<UsuarioProyecto> obtenerMiembrosDelProyecto(Long proyectoId) {
        // Validar que el proyecto existe
        proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyectoId));

        // Usar query con JOIN FETCH para evitar LAZY loading
        return usuarioProyectoRepository.findByProyectoIdWithUsuarioEager(proyectoId);
    }

    @Override
    public Optional<UsuarioProyecto> obtenerRolUsuarioEnProyecto(Long usuarioId, Long proyectoId) {
        return usuarioProyectoRepository.findByUsuario_IdAndProyecto_Id(usuarioId, proyectoId);
    }

    @Override
    public boolean esUsuarioMiembroDelProyecto(Long usuarioId, Long proyectoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);

        if (usuario == null || proyecto == null) {
            return false;
        }

        return usuarioProyectoRepository.existsByUsuarioAndProyecto(usuario, proyecto);
    }

    @Override
    public List<UsuarioProyecto> obtenerMiembrosConRol(Long proyectoId, RolNombre rol) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyectoId));

        return usuarioProyectoRepository.findByProyectoAndRolProyecto(proyecto, rol);
    }
}
