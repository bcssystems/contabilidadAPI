package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Atributo;
import com.bcsystems.contabilidad.domain.AtributoValor;
import com.bcsystems.contabilidad.dto.AtributoRegisterRequest;
import com.bcsystems.contabilidad.dto.AtributoResponse;
import com.bcsystems.contabilidad.dto.AtributoUpdateRequest;
import com.bcsystems.contabilidad.dto.AtributoValorRequest;
import com.bcsystems.contabilidad.dto.AtributoValorResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.exception.RepeatedException;
import com.bcsystems.contabilidad.repository.AtributoRepository;
import com.bcsystems.contabilidad.repository.AtributoValorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtributoServiceImpl implements AtributoService {

    private final AtributoRepository atributoRepository;
    private final AtributoValorRepository atributoValorRepository;

    public AtributoServiceImpl(AtributoRepository atributoRepository,
                               AtributoValorRepository atributoValorRepository) {
        this.atributoRepository = atributoRepository;
        this.atributoValorRepository = atributoValorRepository;
    }

    @Override
    public List<AtributoResponse> listarTodas() {
        return atributoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AtributoResponse> listarActivas() {
        return atributoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AtributoResponse obtenerPorId(Integer id) {
        return toResponse(atributoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Atributo no encontrado con id: " + id)));
    }

    @Override
    @Transactional
    public AtributoResponse crear(AtributoRegisterRequest request) {
        if (atributoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new RepeatedException("Ya existe un atributo con ese nombre");
        }

        Atributo atributo = Atributo.builder()
                .nombre(request.nombre())
                .activo(request.activo() != null ? request.activo() : true)
                .valores(new ArrayList<>())
                .build();

        if (request.valores() != null) {
            for (AtributoValorRequest vr : request.valores()) {
                AtributoValor valor = AtributoValor.builder()
                        .atributo(atributo)
                        .valor(vr.valor())
                        .codigoSku(vr.codigoSku())
                        .activo(vr.activo() != null ? vr.activo() : true)
                        .build();
                atributo.getValores().add(valor);
            }
        }

        atributo = atributoRepository.save(atributo);
        return toResponse(atributo);
    }

    @Override
    @Transactional
    public AtributoResponse actualizar(Integer id, AtributoUpdateRequest request) {
        Atributo atributo = atributoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Atributo no encontrado con id: " + id));

        if (request.nombre() != null) {
            if (atributoRepository.existsByNombreIgnoreCaseAndIdAtributoNot(request.nombre(), id)) {
                throw new RepeatedException("Ya existe otro atributo con ese nombre");
            }
            atributo.setNombre(request.nombre());
        }
        if (request.activo() != null) atributo.setActivo(request.activo());

        if (request.valores() != null) {
            atributo.getValores().clear();
            for (AtributoValorRequest vr : request.valores()) {
                AtributoValor valor = AtributoValor.builder()
                        .atributo(atributo)
                        .valor(vr.valor())
                        .codigoSku(vr.codigoSku())
                        .activo(vr.activo() != null ? vr.activo() : true)
                        .build();
                atributo.getValores().add(valor);
            }
        }

        atributo = atributoRepository.save(atributo);
        return toResponse(atributo);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Atributo atributo = atributoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Atributo no encontrado con id: " + id));
        atributo.setActivo(false);
        atributoRepository.save(atributo);
    }

    private AtributoResponse toResponse(Atributo a) {
        List<AtributoValorResponse> valoresResponse = a.getValores() != null
                ? a.getValores().stream().map(v -> new AtributoValorResponse(
                        v.getIdValor(),
                        a.getIdAtributo(),
                        v.getValor(),
                        v.getCodigoSku(),
                        v.getActivo()
                )).collect(Collectors.toList())
                : List.of();

        return new AtributoResponse(
                a.getIdAtributo(),
                a.getNombre(),
                a.getActivo(),
                valoresResponse
        );
    }
}
