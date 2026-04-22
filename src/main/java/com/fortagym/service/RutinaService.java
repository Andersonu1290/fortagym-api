package com.fortagym.service;

import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.RutinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    public Optional<Rutina> buscarPorUsuario(Usuario usuario) {
        return rutinaRepository.findByUsuario(usuario);
    }

    public Rutina guardar(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    public Optional<Rutina> buscarPorId(Long id) {
        return rutinaRepository.findById(id);
    }

    public void eliminar(Long id) {
        rutinaRepository.deleteById(id);
    }
}
