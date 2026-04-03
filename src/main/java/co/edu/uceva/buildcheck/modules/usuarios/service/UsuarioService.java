package co.edu.uceva.buildcheck.modules.usuarios.service;

import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Guarda un usuario nuevo
    @Transactional
    public Usuario save(Usuario usuario) {
        // Encriptar solo si hay una contraseña presente
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    // Elimina un usuario
    @Transactional
    public void delete(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

    // Busca un usuario por ID
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    // Actualiza un usuario existente
    @Transactional
    public Usuario update(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()
                && !usuario.getPassword().equals(usuarioExistente.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else {
            usuario.setPassword(usuarioExistente.getPassword());
        }

        return usuarioRepository.save(usuario);
    }

    // Lista todos los usuarios
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }
}
