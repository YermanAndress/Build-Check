package co.edu.uceva.buildcheck.modules.proyectos.service;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoServiceImpl implements IProyectoService{
    private final IProyectoRepository proyectoRepository;
    private final MovimientoRepository movimientoRepository;

    public ProyectoServiceImpl(IProyectoRepository proyectoRepository, MovimientoRepository movimientoRepository){
        this.proyectoRepository = proyectoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public Proyecto save(Proyecto proyecto){
        return proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional
    public void delete(Proyecto proyecto){
        if (!movimientoRepository.existsByProyecto(proyecto)) {
            proyectoRepository.delete(proyecto);   
        }else{
            throw new IllegalStateException("No se puede eliminar el proyecto porque tiene movimientos asociados");
        }
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
