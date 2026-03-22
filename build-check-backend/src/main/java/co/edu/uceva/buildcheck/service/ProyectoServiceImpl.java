package main.java.co.edu.uceva.buildcheck.service;

import main.java.co.edu.uceva.buildcheck.model.Proyecto;
import main.java.co.edu.uceva.buildcheck.repository.IProyectoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional(readOnly = true)
    public Page<Proyecto> findAll(Pageable pageable){
        return proyectoRepository.findAll(pageable);
    }
}
