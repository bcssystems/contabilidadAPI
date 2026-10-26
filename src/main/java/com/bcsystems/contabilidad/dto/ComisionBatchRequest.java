package com.bcsystems.contabilidad.dto;

import java.util.List;

public record ComisionBatchRequest(List<ComisionRegisterRequest> comisiones) {
}
