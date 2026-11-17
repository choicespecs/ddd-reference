package com.example.shop.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture rules as executable tests. If someone imports a controller into the domain,
 * or makes Catalog depend on Ordering, the build fails. Documentation that cannot rot.
 */
@AnalyzeClasses(packages = "com.example.shop", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** The domain layer is the core: it must not know about the outer layers or Spring. */
    @ArchTest
    static final ArchRule domain_is_independent = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..", "..web..", "org.springframework..");

    /** Application layer orchestrates the domain but knows nothing about HTTP or JPA specifics. */
    @ArchTest
    static final ArchRule application_does_not_depend_on_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..web..");

    /** Bounded contexts must not form cycles (Ordering → Catalog is allowed; not the reverse too). */
    @ArchTest
    static final ArchRule contexts_are_free_of_cycles = slices()
            .matching("com.example.shop.(*)..")
            .should().beFreeOfCycles();

    /** Catalog never reaches into Ordering. */
    @ArchTest
    static final ArchRule catalog_does_not_know_ordering = noClasses()
            .that().resideInAPackage("com.example.shop.catalog..")
            .should().dependOnClassesThat().resideInAPackage("com.example.shop.ordering..");

    /** Within Ordering, only the anti-corruption layer may touch Catalog. */
    @ArchTest
    static final ArchRule only_acl_touches_catalog = classes()
            .that().resideInAPackage("com.example.shop.catalog..")
            .should().onlyHaveDependentClassesThat().resideOutsideOfPackages(
                    "com.example.shop.ordering.domain..",
                    "com.example.shop.ordering.application..",
                    "com.example.shop.ordering.web..");

    /** Integration events are a neutral contract: they may not depend on any context. */
    @ArchTest
    static final ArchRule integration_events_are_neutral = noClasses()
            .that().resideInAPackage("com.example.shop.integration..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.example.shop.catalog..", "com.example.shop.ordering..");
}
