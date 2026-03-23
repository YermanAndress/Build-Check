package co.edu.uceva.buildcheck.modules.materiales.service;

import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;

import org.springframework.transaction.annotation.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    //  Guarda un material nuevo
    @Transactional
    public Material save(Material material) {
        return materialRepository.save(material);
    }

    //  Elimina un material
    @Transactional
    public void delete(Material material) {
        materialRepository.delete(material);
    }

    //  Busca un material por ID
    @Transactional(readOnly = true)
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    //  Actualiza un material existente
    @Transactional
    public Material update(Material material) {
        return materialRepository.save(material);
    }

    //  Lista todos los materiales
    @Transactional(readOnly = true)
    public List<Material> findAll() {
        return materialRepository.findAll();
    }
}