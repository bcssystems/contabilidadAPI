package com.bcsystems.contabilidad.dto;

import java.util.List;

public record NominaReviewRequest(List<Long> comisionIds, List<Long> deduccionIds) {
}
