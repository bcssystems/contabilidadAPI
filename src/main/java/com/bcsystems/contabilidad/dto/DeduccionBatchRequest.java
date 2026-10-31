package com.bcsystems.contabilidad.dto;

import java.util.List;

public record DeduccionBatchRequest(List<DeduccionRegisterRequest> deducciones) {
}
