package co.edu.uceva.buildcheck.modules.proyecto_invitacion.service;

import co.edu.uceva.buildcheck.modules.proyecto_invitacion.model.ProyectoInvitacion;
import co.edu.uceva.buildcheck.modules.proyecto_invitacion.repository.IProyectoInvitacionRepository;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoInvitacionServiceImpl implements IProyectoInvitacionService {
    
    @Autowired
    private IProyectoInvitacionRepository invitacionRepository;
    
    @Autowired
    private IProyectoRepository proyectoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ProyectoInvitacion generarInvitacion(Long proyectoId, Long usuarioCreadorId, RolNombre rolDefault) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyectoId));
        
        Usuario usuarioCreador = usuarioRepository.findById(usuarioCreadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioCreadorId));
        
        if (usuarioCreador == null) {
            throw new RecursoNoEncontradoException("Usuario null después de fetch con ID: " + usuarioCreadorId);
        }
        
        ProyectoInvitacion invitacion = new ProyectoInvitacion();
        invitacion.setProyecto(proyecto);
        invitacion.setCreatedBy(usuarioCreador);
        invitacion.setRolPorDefecto(rolDefault);
        invitacion.setToken(ProyectoInvitacion.generateToken());
        invitacion.setExpiresAt(LocalDateTime.now().plusDays(10));
        invitacion.setUsosRestantes(7);
        invitacion.setActivo(true);
        
        return invitacionRepository.save(invitacion);
    }

    @Override
    public ProyectoInvitacion validarYUsarToken(String token) {
        ProyectoInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new OperacionNoPermitidaException("Token de invitación inválido o no existe"));
        
        if (!invitacion.isValid()) {
            throw new OperacionNoPermitidaException("Token expirado o inactivo");
        }
        
        if (!invitacion.tieneUsosDisponibles()) {
            throw new OperacionNoPermitidaException("Token sin usos disponibles");
        }
        
        // Decrementar usos
        invitacion.decrementarUsos();
        
        // Si se agotaron los usos, desactivar
        if (invitacion.getUsosRestantes() != null && invitacion.getUsosRestantes() == 0) {
            invitacion.setActivo(false);
        }
        
        return invitacionRepository.save(invitacion);
    }

    @Override
    public Optional<ProyectoInvitacion> obtenerPorToken(String token) {
        return invitacionRepository.findByToken(token);
    }

    @Override
    public void revocarInvitacion(String token) {
        ProyectoInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token no encontrado"));
        
        invitacion.setActivo(false);
        invitacionRepository.save(invitacion);
    }

    @Override
    public List<ProyectoInvitacion> listarInvitacionesPorProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con ID: " + proyectoId));
        
        return invitacionRepository.findByProyectoAndActivo(proyecto, true);
    }

    @Override
    public void limpiarInvitacionesExpiradas() {
        invitacionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}

