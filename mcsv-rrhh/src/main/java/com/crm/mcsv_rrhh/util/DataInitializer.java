package com.crm.mcsv_rrhh.util;

import com.crm.mcsv_rrhh.entity.DriverLicense;
import com.crm.mcsv_rrhh.entity.EducationLevel;
import com.crm.mcsv_rrhh.entity.Gender;
import com.crm.mcsv_rrhh.entity.IdentificationType;
import com.crm.mcsv_rrhh.entity.MaritalStatus;
import com.crm.mcsv_rrhh.entity.EmergencyContactRelationship;
import com.crm.mcsv_rrhh.entity.Profession;
import com.crm.mcsv_rrhh.entity.City;
import com.crm.mcsv_rrhh.entity.Commune;
import com.crm.mcsv_rrhh.entity.Region;
import com.crm.mcsv_rrhh.repository.CityRepository;
import com.crm.mcsv_rrhh.repository.CommuneRepository;
import com.crm.mcsv_rrhh.repository.DriverLicenseRepository;
import com.crm.mcsv_rrhh.repository.EmergencyContactRelationshipRepository;
import com.crm.mcsv_rrhh.repository.RegionRepository;
import com.crm.mcsv_rrhh.repository.EducationLevelRepository;
import com.crm.mcsv_rrhh.repository.GenderRepository;
import com.crm.mcsv_rrhh.repository.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.repository.MaritalStatusRepository;
import com.crm.mcsv_rrhh.repository.ProfessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final IdentificationTypeRepository identificationTypeRepository;
    private final GenderRepository genderRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final DriverLicenseRepository driverLicenseRepository;
    private final ProfessionRepository professionRepository;
    private final EmergencyContactRelationshipRepository emergencyContactRelationshipRepository;
    private final RegionRepository regionRepository;
    private final CommuneRepository communeRepository;
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        initializeIdentificationTypes();
        initializeGenders();
        initializeMaritalStatuses();
        initializeEducationLevels();
        initializeDriverLicenses();
        initializeProfessions();
        initializeEmergencyContactRelationships();
        initializeRegions();
        initializeCommunes();
        initializeCities();
    }

    private void initializeIdentificationTypes() {
        createIdentificationTypeIfNotExists("RUT/RUN");
        createIdentificationTypeIfNotExists("DNI");
        createIdentificationTypeIfNotExists("CPF");
        createIdentificationTypeIfNotExists("SSN");
        createIdentificationTypeIfNotExists("Passport");
        log.info("Identification types initialized.");
    }

    private void createIdentificationTypeIfNotExists(String name) {
        if (identificationTypeRepository.findByName(name).isEmpty()) {
            identificationTypeRepository.save(IdentificationType.builder().name(name).status(true).build());
        }
    }

    private void initializeGenders() {
        createGenderIfNotExists("Masculino");
        createGenderIfNotExists("Femenino");
        createGenderIfNotExists("No binario");
        createGenderIfNotExists("Otro");
        createGenderIfNotExists("Prefiero no decirlo");
        log.info("Genders initialized.");
    }

    private void createGenderIfNotExists(String name) {
        if (genderRepository.findByName(name).isEmpty()) {
            genderRepository.save(Gender.builder().name(name).build());
        }
    }

    private void initializeMaritalStatuses() {
        createMaritalStatusIfNotExists("Soltero/a");
        createMaritalStatusIfNotExists("Casado/a");
        createMaritalStatusIfNotExists("Divorciado/a");
        createMaritalStatusIfNotExists("Viudo/a");
        createMaritalStatusIfNotExists("Conviviente civil");
        createMaritalStatusIfNotExists("Separado/a");
        log.info("Marital statuses initialized.");
    }

    private void createMaritalStatusIfNotExists(String name) {
        if (maritalStatusRepository.findByName(name).isEmpty()) {
            maritalStatusRepository.save(MaritalStatus.builder().name(name).build());
        }
    }

    private void initializeEducationLevels() {
        createEducationLevelIfNotExists("Educación básica");
        createEducationLevelIfNotExists("Educación media");
        createEducationLevelIfNotExists("Técnico nivel medio");
        createEducationLevelIfNotExists("Técnico nivel superior");
        createEducationLevelIfNotExists("Universitaria");
        createEducationLevelIfNotExists("Postgrado");
        log.info("Education levels initialized.");
    }

    private void createEducationLevelIfNotExists(String name) {
        if (educationLevelRepository.findByName(name).isEmpty()) {
            educationLevelRepository.save(EducationLevel.builder().name(name).build());
        }
    }

    private void initializeDriverLicenses() {
        createDriverLicenseIfNotExists("No posee");
        createDriverLicenseIfNotExists("Clase A1");
        createDriverLicenseIfNotExists("Clase A2");
        createDriverLicenseIfNotExists("Clase A3");
        createDriverLicenseIfNotExists("Clase B");
        createDriverLicenseIfNotExists("Clase C");
        createDriverLicenseIfNotExists("Clase D");
        createDriverLicenseIfNotExists("Clase E");
        createDriverLicenseIfNotExists("Clase F");
        log.info("Driver licenses initialized.");
    }

    private void createDriverLicenseIfNotExists(String name) {
        if (driverLicenseRepository.findByName(name).isEmpty()) {
            driverLicenseRepository.save(DriverLicense.builder().name(name).build());
        }
    }

    private void initializeProfessions() {
        // Grupo 1 — Directivos y gerentes
        createProfessionIfNotExists("Gerente General");
        createProfessionIfNotExists("Gerente de Área");
        createProfessionIfNotExists("Director/a de Empresa");
        createProfessionIfNotExists("Jefe/a de Proyecto");
        // Grupo 2 — Profesionales científicos e intelectuales
        createProfessionIfNotExists("Médico/a");
        createProfessionIfNotExists("Enfermero/a");
        createProfessionIfNotExists("Kinesiólogo/a");
        createProfessionIfNotExists("Nutricionista");
        createProfessionIfNotExists("Odontólogo/a");
        createProfessionIfNotExists("Psicólogo/a");
        createProfessionIfNotExists("Químico/a Farmacéutico/a");
        createProfessionIfNotExists("Médico/a Veterinario/a");
        createProfessionIfNotExists("Abogado/a");
        createProfessionIfNotExists("Contador/a Auditor/a");
        createProfessionIfNotExists("Economista");
        createProfessionIfNotExists("Ingeniero/a Civil");
        createProfessionIfNotExists("Ingeniero/a Comercial");
        createProfessionIfNotExists("Ingeniero/a en Informática");
        createProfessionIfNotExists("Ingeniero/a en Construcción");
        createProfessionIfNotExists("Arquitecto/a");
        createProfessionIfNotExists("Profesor/a de Educación Básica");
        createProfessionIfNotExists("Profesor/a de Educación Media");
        createProfessionIfNotExists("Periodista");
        createProfessionIfNotExists("Sociólogo/a");
        createProfessionIfNotExists("Asistente Social");
        createProfessionIfNotExists("Diseñador/a Gráfico/a");
        createProfessionIfNotExists("Diseñador/a Industrial");
        // Grupo 3 — Técnicos y profesionales de nivel medio
        createProfessionIfNotExists("Técnico/a en Enfermería");
        createProfessionIfNotExists("Técnico/a en Informática");
        createProfessionIfNotExists("Técnico/a en Administración");
        createProfessionIfNotExists("Técnico/a en Contabilidad");
        createProfessionIfNotExists("Técnico/a en Electricidad");
        createProfessionIfNotExists("Técnico/a en Construcción");
        createProfessionIfNotExists("Técnico/a en Mecánica");
        createProfessionIfNotExists("Técnico/a en Telecomunicaciones");
        createProfessionIfNotExists("Técnico/a en Refrigeración");
        createProfessionIfNotExists("Técnico/a en Minería");
        createProfessionIfNotExists("Técnico/a en Acuicultura");
        createProfessionIfNotExists("Técnico/a en Laboratorio");
        createProfessionIfNotExists("Técnico/a en Radiología");
        createProfessionIfNotExists("Paramédico/a");
        createProfessionIfNotExists("Topógrafo/a");
        // Grupo 4 — Personal de apoyo administrativo
        createProfessionIfNotExists("Secretario/a");
        createProfessionIfNotExists("Asistente Administrativo/a");
        createProfessionIfNotExists("Recepcionista");
        createProfessionIfNotExists("Digitador/a");
        createProfessionIfNotExists("Asistente de Recursos Humanos");
        createProfessionIfNotExists("Operador/a de Call Center");
        // Grupo 5 — Trabajadores de servicios y vendedores
        createProfessionIfNotExists("Ejecutivo/a de Ventas");
        createProfessionIfNotExists("Vendedor/a");
        createProfessionIfNotExists("Cajero/a");
        createProfessionIfNotExists("Cocinero/a");
        createProfessionIfNotExists("Garzón/Garzona");
        createProfessionIfNotExists("Barista");
        createProfessionIfNotExists("Guardia de Seguridad");
        createProfessionIfNotExists("Auxiliar de Enfermería");
        createProfessionIfNotExists("Peluquero/a");
        createProfessionIfNotExists("Estilista");
        // Grupo 6 — Agricultores y trabajadores calificados agropecuarios
        createProfessionIfNotExists("Agricultor/a");
        createProfessionIfNotExists("Técnico/a Agrícola");
        createProfessionIfNotExists("Trabajador/a Agrícola");
        // Grupo 7 — Oficiales, operarios y artesanos
        createProfessionIfNotExists("Electricista");
        createProfessionIfNotExists("Gasfíter");
        createProfessionIfNotExists("Carpintero/a");
        createProfessionIfNotExists("Soldador/a");
        createProfessionIfNotExists("Pintor/a");
        createProfessionIfNotExists("Mecánico/a Automotriz");
        createProfessionIfNotExists("Mecánico/a Industrial");
        createProfessionIfNotExists("Técnico/a en Refrigeración y Climatización");
        createProfessionIfNotExists("Maestro/a de Obra");
        createProfessionIfNotExists("Albañil");
        // Grupo 8 — Operadores de instalaciones y máquinas
        createProfessionIfNotExists("Operador/a de Maquinaria Pesada");
        createProfessionIfNotExists("Operario/a de Producción");
        createProfessionIfNotExists("Operador/a de Montacargas");
        createProfessionIfNotExists("Conductor/a de Bus");
        createProfessionIfNotExists("Chofer");
        // Grupo 9 — Ocupaciones elementales
        createProfessionIfNotExists("Auxiliar de Aseo");
        createProfessionIfNotExists("Bodeguero/a");
        createProfessionIfNotExists("Reponedor/a");
        createProfessionIfNotExists("Mensajero/a");
        createProfessionIfNotExists("Cargador/a");
        // Sin clasificar / Otro
        createProfessionIfNotExists("Otro");
        log.info("Professions initialized.");
    }

    private void createProfessionIfNotExists(String name) {
        if (professionRepository.findByName(name).isEmpty()) {
            professionRepository.save(Profession.builder().name(name).build());
        }
    }

    private void initializeEmergencyContactRelationships() {
        createEmergencyContactRelationshipIfNotExists("Cónyuge");
        createEmergencyContactRelationshipIfNotExists("Conviviente civil");
        createEmergencyContactRelationshipIfNotExists("Padre");
        createEmergencyContactRelationshipIfNotExists("Madre");
        createEmergencyContactRelationshipIfNotExists("Hijo/a");
        createEmergencyContactRelationshipIfNotExists("Hermano/a");
        createEmergencyContactRelationshipIfNotExists("Abuelo/a");
        createEmergencyContactRelationshipIfNotExists("Tío/a");
        createEmergencyContactRelationshipIfNotExists("Primo/a");
        createEmergencyContactRelationshipIfNotExists("Amigo/a");
        createEmergencyContactRelationshipIfNotExists("Compañero/a de trabajo");
        createEmergencyContactRelationshipIfNotExists("Otro");
        log.info("Emergency contact relationships initialized.");
    }

    private void createEmergencyContactRelationshipIfNotExists(String name) {
        if (emergencyContactRelationshipRepository.findByName(name).isEmpty()) {
            emergencyContactRelationshipRepository.save(EmergencyContactRelationship.builder().name(name).build());
        }
    }

    private void initializeRegions() {
        createRegionIfNotExists("Región de Arica y Parinacota");
        createRegionIfNotExists("Región de Tarapacá");
        createRegionIfNotExists("Región de Antofagasta");
        createRegionIfNotExists("Región de Atacama");
        createRegionIfNotExists("Región de Coquimbo");
        createRegionIfNotExists("Región de Valparaíso");
        createRegionIfNotExists("Región Metropolitana de Santiago");
        createRegionIfNotExists("Región del Libertador General Bernardo O'Higgins");
        createRegionIfNotExists("Región del Maule");
        createRegionIfNotExists("Región de Ñuble");
        createRegionIfNotExists("Región del Biobío");
        createRegionIfNotExists("Región de La Araucanía");
        createRegionIfNotExists("Región de Los Ríos");
        createRegionIfNotExists("Región de Los Lagos");
        createRegionIfNotExists("Región de Aysén del General Carlos Ibáñez del Campo");
        createRegionIfNotExists("Región de Magallanes y de la Antártica Chilena");
        log.info("Regions initialized.");
    }

    private void createRegionIfNotExists(String name) {
        if (regionRepository.findByName(name).isEmpty()) {
            regionRepository.save(Region.builder().name(name).build());
        }
    }

    private void initializeCommunes() {
        createCommunesForRegion("Región de Arica y Parinacota",
                "Arica", "Camarones", "Putre", "General Lagos");

        createCommunesForRegion("Región de Tarapacá",
                "Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica");

        createCommunesForRegion("Región de Antofagasta",
                "Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "Ollagüe", "San Pedro de Atacama", "Tocopilla", "María Elena");

        createCommunesForRegion("Región de Atacama",
                "Copiapó", "Caldera", "Tierra Amarilla", "Chañaral", "Diego de Almagro", "Vallenar", "Alto del Carmen", "Freirina", "Huasco");

        createCommunesForRegion("Región de Coquimbo",
                "La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña", "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "Combarbalá", "Monte Patria", "Punitaqui", "Río Hurtado");

        createCommunesForRegion("Región de Valparaíso",
                "Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero", "Viña del Mar", "Isla de Pascua", "Los Andes", "Calle Larga", "Rinconada", "San Esteban", "La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar", "Quillota", "Calera", "Hijuelas", "La Cruz", "Nogales", "San Antonio", "Algarrobo", "Cartagena", "El Quisco", "El Tabo", "Santo Domingo", "San Felipe", "Catemu", "Llaillay", "Panquehue", "Putaendo", "Santa María", "Quilpué", "Limache", "Olmué", "Villa Alemana");

        createCommunesForRegion("Región Metropolitana de Santiago",
                "Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque", "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San Joaquín", "San Miguel", "San Ramón", "Vitacura", "Puente Alto", "Pirque", "San José de Maipo", "Colina", "Lampa", "Tiltil", "San Bernardo", "Buin", "Calera de Tango", "Paine", "Melipilla", "Alhué", "Curacaví", "María Pinto", "San Pedro", "Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado", "Peñaflor");

        createCommunesForRegion("Región del Libertador General Bernardo O'Higgins",
                "Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras", "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua", "Quinta de Tilcoco", "Rengo", "Requínoa", "San Vicente", "Pichilemu", "La Estrella", "Litueche", "Marchihue", "Navidad", "Paredones", "San Fernando", "Chépica", "Chimbarongo", "Lolol", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz");

        createCommunesForRegion("Región del Maule",
                "Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue", "Río Claro", "San Clemente", "San Rafael", "Cauquenes", "Chanco", "Pelluhue", "Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia", "Teno", "Vichuquén", "Linares", "Colbún", "Longaví", "Parral", "Retiro", "San Javier", "Villa Alegre", "Yerbas Buenas");

        createCommunesForRegion("Región de Ñuble",
                "Chillán", "Bulnes", "Chillán Viejo", "El Carmen", "Pemuco", "Pinto", "Quillón", "San Ignacio", "Yungay", "Quirihue", "Cobquecura", "Coelemu", "Ninhue", "Portezuelo", "Ránquil", "Trehuaco", "San Carlos", "Coihueco", "Ñiquén", "San Fabián", "San Nicolás");

        createCommunesForRegion("Región del Biobío",
                "Concepción", "Coronel", "Chiguayante", "Florida", "Hualqui", "Lota", "Penco", "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé", "Hualpén", "Lebu", "Arauco", "Cañete", "Contulmo", "Curanilahue", "Los Álamos", "Tirúa", "Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete", "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel", "Alto Biobío");

        createCommunesForRegion("Región de La Araucanía",
                "Temuco", "Carahue", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea", "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco", "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén", "Vilcún", "Villarrica", "Cholchol", "Angol", "Collipulli", "Curacautín", "Ercilla", "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén", "Victoria");

        createCommunesForRegion("Región de Los Ríos",
                "Valdivia", "Corral", "Futrono", "La Unión", "Lago Ranco", "Lanco", "Los Lagos", "Máfil", "Mariquina", "Paillaco", "Panguipulli", "Río Bueno");

        createCommunesForRegion("Región de Los Lagos",
                "Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos", "Llanquihue", "Maullín", "Puerto Varas", "Castro", "Ancud", "Chonchi", "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi", "Quinchao", "Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro", "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú", "Hualaihué", "Palena");

        createCommunesForRegion("Región de Aysén del General Carlos Ibáñez del Campo",
                "Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "Cochrane", "O'Higgins", "Tortel", "Chile Chico", "Río Ibáñez");

        createCommunesForRegion("Región de Magallanes y de la Antártica Chilena",
                "Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio", "Cabo de Hornos", "Antártica", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine");

        log.info("Communes initialized.");
    }

    private void createCommunesForRegion(String regionName, String... communeNames) {
        regionRepository.findByName(regionName).ifPresent(region -> {
            for (String communeName : communeNames) {
                if (communeRepository.findByNameAndRegionId(communeName, region.getId()).isEmpty()) {
                    communeRepository.save(Commune.builder().name(communeName).regionId(region.getId()).build());
                }
            }
        });
    }

    private void initializeCities() {
        // Las ciudades son localidades dentro de cada comuna.
        // Se agregan las principales capitales comunales como referencia.
        // El frontend filtra ciudades por communeId.
        createCitiesForCommune("Región de Arica y Parinacota", "Arica", "Arica");
        createCitiesForCommune("Región de Tarapacá", "Iquique", "Iquique");
        createCitiesForCommune("Región de Tarapacá", "Alto Hospicio", "Alto Hospicio");
        createCitiesForCommune("Región de Antofagasta", "Antofagasta", "Antofagasta");
        createCitiesForCommune("Región de Antofagasta", "Calama", "Calama");
        createCitiesForCommune("Región de Atacama", "Copiapó", "Copiapó");
        createCitiesForCommune("Región de Atacama", "Vallenar", "Vallenar");
        createCitiesForCommune("Región de Coquimbo", "La Serena", "La Serena");
        createCitiesForCommune("Región de Coquimbo", "Coquimbo", "Coquimbo");
        createCitiesForCommune("Región de Coquimbo", "Ovalle", "Ovalle");
        createCitiesForCommune("Región de Valparaíso", "Valparaíso", "Valparaíso");
        createCitiesForCommune("Región de Valparaíso", "Viña del Mar", "Viña del Mar");
        createCitiesForCommune("Región de Valparaíso", "Quilpué", "Quilpué");
        createCitiesForCommune("Región de Valparaíso", "Villa Alemana", "Villa Alemana");
        createCitiesForCommune("Región de Valparaíso", "San Antonio", "San Antonio");
        createCitiesForCommune("Región Metropolitana de Santiago", "Santiago", "Santiago");
        createCitiesForCommune("Región Metropolitana de Santiago", "Puente Alto", "Puente Alto");
        createCitiesForCommune("Región Metropolitana de Santiago", "Maipú", "Maipú");
        createCitiesForCommune("Región Metropolitana de Santiago", "La Florida", "La Florida");
        createCitiesForCommune("Región Metropolitana de Santiago", "Las Condes", "Las Condes");
        createCitiesForCommune("Región Metropolitana de Santiago", "Ñuñoa", "Ñuñoa");
        createCitiesForCommune("Región Metropolitana de Santiago", "Providencia", "Providencia");
        createCitiesForCommune("Región Metropolitana de Santiago", "San Bernardo", "San Bernardo");
        createCitiesForCommune("Región del Libertador General Bernardo O'Higgins", "Rancagua", "Rancagua");
        createCitiesForCommune("Región del Libertador General Bernardo O'Higgins", "San Fernando", "San Fernando");
        createCitiesForCommune("Región del Maule", "Talca", "Talca");
        createCitiesForCommune("Región del Maule", "Curicó", "Curicó");
        createCitiesForCommune("Región del Maule", "Linares", "Linares");
        createCitiesForCommune("Región de Ñuble", "Chillán", "Chillán");
        createCitiesForCommune("Región del Biobío", "Concepción", "Concepción");
        createCitiesForCommune("Región del Biobío", "Talcahuano", "Talcahuano");
        createCitiesForCommune("Región del Biobío", "Los Ángeles", "Los Ángeles");
        createCitiesForCommune("Región del Biobío", "San Pedro de la Paz", "San Pedro de la Paz");
        createCitiesForCommune("Región de La Araucanía", "Temuco", "Temuco");
        createCitiesForCommune("Región de La Araucanía", "Padre Las Casas", "Padre Las Casas");
        createCitiesForCommune("Región de La Araucanía", "Villarrica", "Villarrica");
        createCitiesForCommune("Región de Los Ríos", "Valdivia", "Valdivia");
        createCitiesForCommune("Región de Los Lagos", "Puerto Montt", "Puerto Montt");
        createCitiesForCommune("Región de Los Lagos", "Osorno", "Osorno");
        createCitiesForCommune("Región de Los Lagos", "Castro", "Castro");
        createCitiesForCommune("Región de Los Lagos", "Puerto Varas", "Puerto Varas");
        createCitiesForCommune("Región de Aysén del General Carlos Ibáñez del Campo", "Coyhaique", "Coyhaique");
        createCitiesForCommune("Región de Magallanes y de la Antártica Chilena", "Punta Arenas", "Punta Arenas");
        createCitiesForCommune("Región de Magallanes y de la Antártica Chilena", "Natales", "Puerto Natales");
        log.info("Cities initialized.");
    }

    private void createCitiesForCommune(String regionName, String communeName, String... cityNames) {
        regionRepository.findByName(regionName).ifPresent(region ->
            communeRepository.findByNameAndRegionId(communeName, region.getId()).ifPresent(commune -> {
                for (String cityName : cityNames) {
                    if (cityRepository.findByNameAndCommuneId(cityName, commune.getId()).isEmpty()) {
                        cityRepository.save(City.builder().name(cityName).communeId(commune.getId()).build());
                    }
                }
            })
        );
    }
}
