package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.SucursalRegisterRequest;
import com.bcsystems.contabilidad.dto.SucursalUpdateRequest;
import com.bcsystems.contabilidad.dto.SucursalResponse;

import java.util.List;

public interface SucursalService {

    List<SucursalResponse> listarTodas();

    List<SucursalResponse> listarActivas();

    SucursalResponse obtenerPorId(Integer id);

    SucursalResponse crear(SucursalRegisterRequest request);

    SucursalResponse actualizar(Integer id, SucursalUpdateRequest request);

    void eliminar(Integer id);
}
