package co.edu.uceva.buildcheck.modules.proyectos.service;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoServiceImpl implements IProyectoService{
    IProyectoRepository proyectoRepository;

    public ProyectoServiceImpl(IProyectoRepository proyectoRepository){
        this.proyectoRepository = proyectoRepository;
    }

    @Override
    @Transactional
    public Proyecto save(Proyecto proyecto){
        return proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional
    public void delete(Proyecto proyecto){
        proyectoRepository.delete(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proyecto> findById(Long id){
        return proyectoRepository.findById(id);
    }

    @Override
    @Transactional
    public Proyecto update(Proyecto proyecto){
        return proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proyecto> findAll(){
        return proyectoRepository.findAll();
    }
}
