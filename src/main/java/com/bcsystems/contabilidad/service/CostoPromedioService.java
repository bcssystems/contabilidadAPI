package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.CostoPromedioResponse;

public interface CostoPromedioService {

    CostoPromedioResponse getCostoPromedioByProductoPadreId(Long productoPadreId);

    CostoPromedioResponse recalcular(Long productoPadreId);
}
