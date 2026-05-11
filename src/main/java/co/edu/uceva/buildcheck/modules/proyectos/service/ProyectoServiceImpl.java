package co.edu.uceva.buildcheck.modules.proyectos.service;

import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.service.IUsuarioProyectoService;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProyectoServiceImpl implements IProyectoService {
    private final IProyectoRepository proyectoRepository;
    private final MovimientoRepository movimientoRepository;
    private final IUsuarioProyectoService usuarioProyectoService;
    private final UsuarioRepository usuarioRepository;

    public ProyectoServiceImpl(IProyectoRepository proyectoRepository, 
                            MovimientoRepository movimientoRepository,
                            IUsuarioProyectoService usuarioProyectoService,
                            UsuarioRepository usuarioRepository) {
        this.proyectoRepository = proyectoRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioProyectoService = usuarioProyectoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Proyecto save(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional
    public void delete(Proyecto proyecto) {
        if (!movimientoRepository.existsByProyecto(proyecto)) {
            proyectoRepository.delete(proyecto);
        } else {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el proyecto porque tiene movimientos asociados");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proyecto> findById(Long id) {
        return proyectoRepository.findById(id);
    }

    @Override
    @Transactional
    public Proyecto update(Proyecto proyecto) {
        Proyecto proyectoExistente = proyectoRepository.findById(proyecto.getId())
                .orElseThrow(
                        () -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyecto.getId()));
        proyectoExistente.setNombre(proyecto.getNombre());
        proyectoExistente.setDescripcion(proyecto.getDescripcion());
        proyectoExistente.setEstado(proyecto.getEstado());
        proyectoExistente.setPresupuesto(proyecto.getPresupuesto());
        proyectoExistente.setUbicacion(proyecto.getUbicacion());
        return proyectoRepository.save(proyectoExistente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proyecto> findAll() {
        return proyectoRepository.findAll();
    }

    @Override
    @Transactional
    public Proyecto crearProyecto(Proyecto proyecto, Long usuarioCreadorId) {
        // Obtener usuario
        var usuario = usuarioRepository.findById(usuarioCreadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioCreadorId));
        
        // Establecer usuario propietario
        proyecto.setUsuarioPropietario(usuario);
        
        // Guardar proyecto
        Proyecto nuevoProyecto = proyectoRepository.save(proyecto);
        
        // Agregar usuario como OWNER del proyecto
        usuarioProyectoService.agregarUsuarioAlProyecto(usuarioCreadorId, nuevoProyecto.getId(), RolNombre.ROLE_OWNER);
        
        return nuevoProyecto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proyecto> obtenerProyectosDelUsuario(Long usuarioId) {
        // Obtener todos los UsuarioProyecto del usuario
        var usuarioProyectos = usuarioProyectoService.obtenerProyectosDelUsuario(usuarioId);
        
        // Extraer los proyectos
        return usuarioProyectos.stream()
                .map(up -> up.getProyecto())
                .collect(Collectors.toList());
    }
}
