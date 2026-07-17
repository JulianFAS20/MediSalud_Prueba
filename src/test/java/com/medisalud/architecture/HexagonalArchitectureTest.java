package com.medisalud.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final ArchRule DOMINIO_ES_INDEPENDIENTE = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..infrastructure..",
                    "org.springframework..",
                    "jakarta..");

    private static final ArchRule APLICACION_NO_CONOCE_INFRAESTRUCTURA = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infrastructure..",
                    "org.springframework..",
                    "jakarta..");

    @Test
    void debeRespetarLasDependenciasHexagonales() {
        JavaClasses clases = new ClassFileImporter().importPackages("com.medisalud");
        DOMINIO_ES_INDEPENDIENTE.check(clases);
        APLICACION_NO_CONOCE_INFRAESTRUCTURA.check(clases);
    }
}
