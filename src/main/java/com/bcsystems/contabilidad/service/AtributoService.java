package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.AtributoRegisterRequest;
import com.bcsystems.contabilidad.dto.AtributoResponse;
import com.bcsystems.contabilidad.dto.AtributoUpdateRequest;

import java.util.List;

public interface AtributoService {

    List<AtributoResponse> listarTodas();

    List<AtributoResponse> listarActivas();

    AtributoResponse obtenerPorId(Integer id);

    AtributoResponse crear(AtributoRegisterRequest request);

    AtributoResponse actualizar(Integer id, AtributoUpdateRequest request);

    void eliminar(Integer id);
}
