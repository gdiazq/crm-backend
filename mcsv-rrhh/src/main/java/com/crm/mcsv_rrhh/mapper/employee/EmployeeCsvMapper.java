package com.crm.mcsv_rrhh.mapper.employee;

import com.crm.common.util.CsvUtil;
import com.crm.mcsv_rrhh.dto.employee.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.entity.city.City;
import com.crm.mcsv_rrhh.entity.commune.Commune;
import com.crm.mcsv_rrhh.mapper.shared.CatalogResolver;
import com.crm.mcsv_rrhh.repository.afp.AfpRepository;
import com.crm.mcsv_rrhh.repository.bank.BankRepository;
import com.crm.mcsv_rrhh.repository.city.CityRepository;
import com.crm.mcsv_rrhh.repository.commune.CommuneRepository;
import com.crm.mcsv_rrhh.repository.driverlicense.DriverLicenseRepository;
import com.crm.mcsv_rrhh.repository.educationlevel.EducationLevelRepository;
import com.crm.mcsv_rrhh.repository.emergencycontactrelationship.EmergencyContactRelationshipRepository;
import com.crm.mcsv_rrhh.repository.expat.ExpatRepository;
import com.crm.mcsv_rrhh.repository.familyallowancetier.FamilyAllowanceTierRepository;
import com.crm.mcsv_rrhh.repository.gender.GenderRepository;
import com.crm.mcsv_rrhh.repository.healthinsurance.HealthInsuranceRepository;
import com.crm.mcsv_rrhh.repository.healthinsurancetariff.HealthInsuranceTariffRepository;
import com.crm.mcsv_rrhh.repository.identificationtype.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.repository.maritalstatus.MaritalStatusRepository;
import com.crm.mcsv_rrhh.repository.nationality.NationalityRepository;
import com.crm.mcsv_rrhh.repository.paymentmethod.PaymentMethodRepository;
import com.crm.mcsv_rrhh.repository.pensionstatus.PensionStatusRepository;
import com.crm.mcsv_rrhh.repository.profession.ProfessionRepository;
import com.crm.mcsv_rrhh.repository.region.RegionRepository;
import com.crm.mcsv_rrhh.repository.retirementstatus.RetirementStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapea una fila del CSV de importación masiva al request del formulario de creación de empleado.
 * Las columnas de catálogo viajan por nombre (como las ve el usuario en los selects del form) y se
 * resuelven vía {@link CatalogResolver#idByName}; si el nombre no existe, la fila falla con mensaje claro.
 */
@Component
@RequiredArgsConstructor
public class EmployeeCsvMapper {

    private final IdentificationTypeRepository identificationTypeRepository;
    private final GenderRepository genderRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final DriverLicenseRepository driverLicenseRepository;
    private final ProfessionRepository professionRepository;
    private final EmergencyContactRelationshipRepository emergencyContactRelationshipRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final CommuneRepository communeRepository;
    private final ExpatRepository expatRepository;
    private final NationalityRepository nationalityRepository;
    private final FamilyAllowanceTierRepository familyAllowanceTierRepository;
    private final RetirementStatusRepository retirementStatusRepository;
    private final PensionStatusRepository pensionStatusRepository;
    private final AfpRepository afpRepository;
    private final HealthInsuranceRepository healthInsuranceRepository;
    private final HealthInsuranceTariffRepository healthInsuranceTariffRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BankRepository bankRepository;

    /** Valida que el header tenga al menos las columnas mínimas (rut o nombre). */
    public boolean hasRecognizedColumns(Map<String, Integer> idx) {
        return idx.containsKey("rut") || idx.containsKey("nombre");
    }

    /** Arma el request del form a partir de una fila, resolviendo catálogos por nombre. */
    public CreateEmployeeRequest fromCsvRow(String[] cols, Map<String, Integer> idx) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();

        // ─── Datos Personales ─────────────────────────────────────────────────
        request.setIdentification(CsvUtil.text(cols, idx, "rut"));
        request.setIdentificationTypeId(catalog(CsvUtil.text(cols, idx, "tipo identificación", "tipo identificacion"), identificationTypeRepository, "Tipo de identificación"));
        request.setFirstName(CsvUtil.text(cols, idx, "nombre"));
        request.setPaternalLastName(CsvUtil.text(cols, idx, "apellido paterno"));
        request.setMaternalLastName(CsvUtil.text(cols, idx, "apellido materno"));
        request.setBirthDate(CsvUtil.parseDate(CsvUtil.text(cols, idx, "fecha nacimiento")));
        request.setGenderId(catalog(CsvUtil.text(cols, idx, "género", "genero"), genderRepository, "Género"));
        request.setMaritalStatusId(catalog(CsvUtil.text(cols, idx, "estado civil"), maritalStatusRepository, "Estado civil"));
        request.setEducationLevelId(catalog(CsvUtil.text(cols, idx, "nivel educacional"), educationLevelRepository, "Nivel educacional"));
        request.setDriverLicenseId(catalog(CsvUtil.text(cols, idx, "licencia conducir"), driverLicenseRepository, "Licencia de conducir"));
        request.setProfessionId(catalog(CsvUtil.text(cols, idx, "profesión", "profesion"), professionRepository, "Profesión"));

        // ─── Datos de Contacto ────────────────────────────────────────────────
        request.setPersonalEmail(CsvUtil.text(cols, idx, "email personal"));
        request.setCorporateEmail(CsvUtil.text(cols, idx, "email corporativo"));
        request.setPhone(CsvUtil.text(cols, idx, "teléfono", "telefono"));
        request.setPhone2(CsvUtil.text(cols, idx, "teléfono 2", "telefono 2"));

        // ─── Contacto de Emergencia ───────────────────────────────────────────
        request.setEmergencyContactName(CsvUtil.text(cols, idx, "contacto emergencia"));
        request.setEmergencyContactRelationshipId(catalog(CsvUtil.text(cols, idx, "parentesco emergencia"), emergencyContactRelationshipRepository, "Parentesco"));
        request.setEmergencyContactPhone(CsvUtil.text(cols, idx, "teléfono emergencia", "telefono emergencia"));
        request.setEmergencyContactPhone2(CsvUtil.text(cols, idx, "teléfono emergencia 2", "telefono emergencia 2"));

        // ─── Dirección ────────────────────────────────────────────────────────
        request.setStreetName(CsvUtil.text(cols, idx, "calle"));
        request.setStreetNumber(CsvUtil.text(cols, idx, "número", "numero"));
        request.setPostalCode(CsvUtil.text(cols, idx, "código postal", "codigo postal"));
        request.setDepartment(CsvUtil.text(cols, idx, "departamento"));
        request.setVillage(CsvUtil.text(cols, idx, "villa"));
        request.setBlock(CsvUtil.text(cols, idx, "block"));
        request.setRegionId(catalog(CsvUtil.text(cols, idx, "región", "region"), regionRepository, "Región"));
        request.setCommuneId(resolveCommune(CsvUtil.text(cols, idx, "comuna"), request.getRegionId()));
        request.setCityId(resolveCity(CsvUtil.text(cols, idx, "ciudad"), request.getCommuneId()));

        // ─── Previsión y Salud ────────────────────────────────────────────────
        request.setExpatId(catalog(CsvUtil.text(cols, idx, "expatriado"), expatRepository, "Expatriado"));
        request.setNationalityId(catalog(CsvUtil.text(cols, idx, "nacionalidad"), nationalityRepository, "Nacionalidad"));
        request.setFamilyAllowanceTierId(catalog(CsvUtil.text(cols, idx, "tramo asignación familiar", "tramo asignacion familiar"), familyAllowanceTierRepository, "Tramo asignación familiar"));
        request.setRetirementStatusId(catalog(CsvUtil.text(cols, idx, "estado jubilación", "estado jubilacion"), retirementStatusRepository, "Estado de jubilación"));
        request.setIsapreFun(CsvUtil.text(cols, idx, "fun isapre"));
        request.setPensionStatusId(catalog(CsvUtil.text(cols, idx, "estado pensión", "estado pension"), pensionStatusRepository, "Estado de pensión"));
        request.setAfpId(catalog(CsvUtil.text(cols, idx, "afp"), afpRepository, "AFP"));
        request.setHealthInsuranceId(catalog(CsvUtil.text(cols, idx, "previsión salud", "prevision salud"), healthInsuranceRepository, "Previsión de salud"));
        request.setHealthInsuranceTariffId(catalog(CsvUtil.text(cols, idx, "plan salud"), healthInsuranceTariffRepository, "Plan de salud"));
        request.setHealthInsuranceUF(CsvUtil.text(cols, idx, "monto uf"));
        request.setHealthInsurancePesos(CsvUtil.text(cols, idx, "monto pesos"));

        // ─── Forma de Pago ────────────────────────────────────────────────────
        request.setPaymentMethodId(catalog(CsvUtil.text(cols, idx, "forma pago"), paymentMethodRepository, "Forma de pago"));
        request.setBankId(catalog(CsvUtil.text(cols, idx, "banco"), bankRepository, "Banco"));
        request.setBankAccount(CsvUtil.text(cols, idx, "cuenta bancaria"));

        // ─── Otros Datos ──────────────────────────────────────────────────────
        request.setClothingSize(CsvUtil.text(cols, idx, "talla ropa"));
        request.setShoeSize(CsvUtil.text(cols, idx, "talla zapato"));
        request.setPantSize(CsvUtil.text(cols, idx, "talla pantalón", "talla pantalon"));
        request.setRehireEligible(CsvUtil.parseBoolean(CsvUtil.text(cols, idx, "recontratable")));

        return request;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Id del catálogo por nombre; {@code null} si viene vacío, error si el nombre no existe. */
    private static Long catalog(String value, Object repository, String label) {
        if (value == null) return null;
        return CatalogResolver.idByName(value, repository)
                .orElseThrow(() -> new IllegalArgumentException(label + " no encontrado: " + value));
    }

    /** Comuna se busca dentro de su región (el catálogo es jerárquico). */
    private Long resolveCommune(String name, Long regionId) {
        if (name == null) return null;
        if (regionId == null)
            throw new IllegalArgumentException("Para indicar Comuna debe venir también la columna Región");
        return communeRepository.findByNameAndRegionId(name, regionId)
                .map(Commune::getId)
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada en la región: " + name));
    }

    /** Ciudad se busca dentro de su comuna (el catálogo es jerárquico). */
    private Long resolveCity(String name, Long communeId) {
        if (name == null) return null;
        if (communeId == null)
            throw new IllegalArgumentException("Para indicar Ciudad debe venir también la columna Comuna");
        return cityRepository.findByNameAndCommuneId(name, communeId)
                .map(City::getId)
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada en la comuna: " + name));
    }

}
