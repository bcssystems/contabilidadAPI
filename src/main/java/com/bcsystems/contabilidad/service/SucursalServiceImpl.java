package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Sucursal;
import com.bcsystems.contabilidad.dto.SucursalRegisterRequest;
import com.bcsystems.contabilidad.dto.SucursalResponse;
import com.bcsystems.contabilidad.dto.SucursalUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.exception.RepeatedException;
import com.bcsystems.contabilidad.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalServiceImpl(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    @Override
    public List<SucursalResponse> listarTodas() {
        return sucursalRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SucursalResponse> listarActivas() {
        return sucursalRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SucursalResponse obtenerPorId(Integer id) {
        return toResponse(sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id)));
    }

    @Override
    @Transactional
    public SucursalResponse crear(SucursalRegisterRequest request) {
        if (sucursalRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new RepeatedException("Ya existe una sucursal con ese nombre");
        }

        Sucursal sucursal = Sucursal.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .activa(request.activa() != null ? request.activa() : true)
                .build();

        sucursal = sucursalRepository.save(sucursal);
        return toResponse(sucursal);
    }

    @Override
    @Transactional
    public SucursalResponse actualizar(Integer id, SucursalUpdateRequest request) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id));

        if (sucursalRepository.existsByNombreIgnoreCaseAndIdSucursalNot(request.nombre(), id)) {
            throw new RepeatedException("Ya existe otra sucursal con ese nombre");
        }

        sucursal.setNombre(request.nombre());
        sucursal.setDireccion(request.direccion());
        sucursal.setTelefono(request.telefono());
        if (request.activa() != null) sucursal.setActiva(request.activa());

        sucursalRepository.save(sucursal);
        return toResponse(sucursal);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada con id: " + id));
        sucursal.setActiva(false);
        sucursalRepository.save(sucursal);
    }

    private SucursalResponse toResponse(Sucursal s) {
        return new SucursalResponse(
                s.getIdSucursal(),
                s.getNombre(),
                s.getDireccion(),
                s.getTelefono(),
                s.getActiva()
        );
    }
}
