package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.en.TipoCuentaContable;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CatCuentasInitializer {

    private final CuentaContableRepository cuentaContableRepository;

    public CatCuentasInitializer(CuentaContableRepository cuentaContableRepository) {
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Bean
    public CommandLineRunner initCuentas() {
        return args -> {
            if (cuentaContableRepository.count() > 0) {
                return;
            }

            CuentaContable activo = CuentaContable.builder()
                    .codigo("1")
                    .nombre("ACTIVO")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .build();
            activo = cuentaContableRepository.save(activo);

            CuentaContable activoCirculante = CuentaContable.builder()
                    .codigo("11")
                    .nombre("ACTIVO CIRCULANTE")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activo)
                    .build();
            activoCirculante = cuentaContableRepository.save(activoCirculante);

            CuentaContable caja = CuentaContable.builder()
                    .codigo("1101")
                    .nombre("CAJA")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activoCirculante)
                    .build();
            cuentaContableRepository.save(caja);

            CuentaContable bancos = CuentaContable.builder()
                    .codigo("1102")
                    .nombre("BANCOS")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activoCirculante)
                    .build();
            bancos = cuentaContableRepository.save(bancos);

            cuentaContableRepository.save(CuentaContable.builder()
                    .codigo("1102.01")
                    .nombre("BBVA 8777")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(bancos)
                    .build());

            cuentaContableRepository.save(CuentaContable.builder()
                    .codigo("1102.02")
                    .nombre("BANCOPPEL 3091")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(bancos)
                    .build());

            cuentaContableRepository.save(CuentaContable.builder()
                    .codigo("1102.03")
                    .nombre("MERCADO PAGO 3208")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(bancos)
                    .build());

            CuentaContable clientes = CuentaContable.builder()
                    .codigo("1103")
                    .nombre("CLIENTES")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activoCirculante)
                    .build();
            cuentaContableRepository.save(clientes);

            CuentaContable deudores = CuentaContable.builder()
                    .codigo("1104")
                    .nombre("DEUDORES DIVERSOS")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activoCirculante)
                    .build();
            cuentaContableRepository.save(deudores);

            CuentaContable almacen = CuentaContable.builder()
                    .codigo("1105")
                    .nombre("ALMACEN")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activoCirculante)
                    .build();
            cuentaContableRepository.save(almacen);

            CuentaContable activoFijo = CuentaContable.builder()
                    .codigo("12")
                    .nombre("ACTIVO FIJO")
                    .tipoCuentaContable(TipoCuentaContable.ACTIVO)
                    .activa(true)
                    .cuentaPadre(activo)
                    .build();
            activoFijo = cuentaContableRepository.save(activoFijo);

            cuentaContableRepository.saveAll(List.of(
                    CuentaContable.builder().codigo("1201").nombre("MOBILIARIO Y EQUIPO").tipoCuentaContable(TipoCuentaContable.ACTIVO).activa(true).cuentaPadre(activoFijo).build(),
                    CuentaContable.builder().codigo("1202").nombre("EQUIPO DE TRANSPORTE").tipoCuentaContable(TipoCuentaContable.ACTIVO).activa(true).cuentaPadre(activoFijo).build(),
                    CuentaContable.builder().codigo("1203").nombre("EQUIPO DE COMPUTO").tipoCuentaContable(TipoCuentaContable.ACTIVO).activa(true).cuentaPadre(activoFijo).build(),
                    CuentaContable.builder().codigo("1204").nombre("EQUIPO DE OFICINA").tipoCuentaContable(TipoCuentaContable.ACTIVO).activa(true).cuentaPadre(activoFijo).build()
            ));

            CuentaContable pasivo = CuentaContable.builder()
                    .codigo("2")
                    .nombre("PASIVO")
                    .tipoCuentaContable(TipoCuentaContable.PASIVO)
                    .activa(true)
                    .build();
            pasivo = cuentaContableRepository.save(pasivo);

            CuentaContable provedores = CuentaContable.builder()
                    .codigo("21")
                    .nombre("PROVEEDORES")
                    .tipoCuentaContable(TipoCuentaContable.PASIVO)
                    .activa(true)
                    .cuentaPadre(pasivo)
                    .build();
            provedores = cuentaContableRepository.save(provedores);

            cuentaContableRepository.saveAll(List.of(
                    CuentaContable.builder().codigo("21.01").nombre("ALEX OR").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.02").nombre("DASSAEL SAN JULIAN").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.03").nombre("ARIEL CONSIGNA").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.04").nombre("YULI CONSIGNA").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.05").nombre("OMAR CONSIGNA").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.06").nombre("TECNOMAXX").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.07").nombre("GOYO SOFTWARE").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build(),
                    CuentaContable.builder().codigo("21.08").nombre("COMERSOL").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(provedores).build()
            ));

            CuentaContable acrBancarios = CuentaContable.builder()
                    .codigo("22")
                    .nombre("ACREEDORES BANCARIOS")
                    .tipoCuentaContable(TipoCuentaContable.PASIVO)
                    .activa(true)
                    .cuentaPadre(pasivo)
                    .build();
            acrBancarios = cuentaContableRepository.save(acrBancarios);

            cuentaContableRepository.saveAll(List.of(
                    CuentaContable.builder().codigo("22.01").nombre("MERCADO PAGO OMAR 5298").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.02").nombre("NU OMAR 7310").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.03").nombre("BANAMEX ORO SANDY 2669").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.04").nombre("BANAMEX COSTCO SANDY 1055").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.05").nombre("BANAMEX JOY IVAN 2527").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.06").nombre("BANORTE IVAN ORO 0337").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.07").nombre("BANORTE IVAN ORO 9939").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.08").nombre("BANORTE CLASICA YULI 0974").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.09").nombre("BANORTE ORO YULI 6731").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.10").nombre("BANAMEX JOY YULI 4029").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build(),
                    CuentaContable.builder().codigo("22.11").nombre("CREDITO BANBAJIO SHALMA").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrBancarios).build()
            ));

            CuentaContable acrDiversos = CuentaContable.builder()
                    .codigo("23")
                    .nombre("ACREEDORES DIVERSOS")
                    .tipoCuentaContable(TipoCuentaContable.PASIVO)
                    .activa(true)
                    .cuentaPadre(pasivo)
                    .build();
            cuentaContableRepository.save(acrDiversos);

            cuentaContableRepository.saveAll(List.of(
                    CuentaContable.builder().codigo("23.01").nombre("SHALMA SOTELO").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrDiversos).build(),
                    CuentaContable.builder().codigo("23.02").nombre("JOSE SOTELO").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrDiversos).build(),
                    CuentaContable.builder().codigo("23.03").nombre("MONTSERRAT OLIVARES").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrDiversos).build(),
                    CuentaContable.builder().codigo("23.04").nombre("OMAR SOTELO").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrDiversos).build(),
                    CuentaContable.builder().codigo("23.05").nombre("IVAN SOTELO").tipoCuentaContable(TipoCuentaContable.PASIVO).activa(true).cuentaPadre(acrDiversos).build()
            ));

            CuentaContable capital = CuentaContable.builder()
                    .codigo("3")
                    .nombre("CAPITAL")
                    .tipoCuentaContable(TipoCuentaContable.CAPITAL)
                    .activa(true)
                    .build();
            cuentaContableRepository.save(capital);

            CuentaContable ingresos = CuentaContable.builder()
                    .codigo("4")
                    .nombre("INGRESOS")
                    .tipoCuentaContable(TipoCuentaContable.INGRESO)
                    .activa(true)
                    .build();
            ingresos = cuentaContableRepository.save(ingresos);

            cuentaContableRepository.saveAll(List.of(
                    CuentaContable.builder().codigo("4101").nombre("VENTAS AL CONTADO").tipoCuentaContable(TipoCuentaContable.INGRESO).activa(true).cuentaPadre(ingresos).build(),
                    CuentaContable.builder().codigo("4102").nombre("VENTAS A CREDITO").tipoCuentaContable(TipoCuentaContable.INGRESO).activa(true).cuentaPadre(ingresos).build()
            ));

            CuentaContable costos = CuentaContable.builder()
                    .codigo("5")
                    .nombre("COSTOS")
                    .tipoCuentaContable(TipoCuentaContable.GASTO)
                    .activa(true)
                    .build();
            costos = cuentaContableRepository.save(costos);

            cuentaContableRepository.save(CuentaContable.builder()
                    .codigo("5101")
                    .nombre("COSTO DE MERCANCIA")
                    .tipoCuentaContable(TipoCuentaContable.GASTO)
                    .activa(true)
                    .cuentaPadre(costos)
                    .build());

            CuentaContable gastos = CuentaContable.builder()
                    .codigo("6")
                    .nombre("GASTOS")
                    .tipoCuentaContable(TipoCuentaContable.GASTO)
                    .activa(true)
                    .build();
            gastos = cuentaContableRepository.save(gastos);

            cuentaContableRepository.save(CuentaContable.builder()
                    .codigo("6101")
                    .nombre("GASTOS OPERACION")
                    .tipoCuentaContable(TipoCuentaContable.GASTO)
                    .activa(true)
                    .cuentaPadre(gastos)
                    .build());

            System.out.println("=== CATALOGO DE CUENTAS INICIALIZADO ===");
        };
    }
}