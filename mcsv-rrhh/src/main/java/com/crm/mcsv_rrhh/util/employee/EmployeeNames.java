package com.crm.mcsv_rrhh.util.employee;

import com.crm.mcsv_rrhh.entity.employee.Employee;

/** Helpers de formato de nombre de empleado, reutilizables por mappers y services. */
public final class EmployeeNames {

    private EmployeeNames() {}

    /** Nombre completo: nombre + apellido paterno + apellido materno, omitiendo los nulos. */
    public static String full(Employee employee) {
        if (employee == null) return null;
        return String.join(" ",
                safe(employee.getFirstName()),
                safe(employee.getPaternalLastName()),
                safe(employee.getMaternalLastName())).trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
