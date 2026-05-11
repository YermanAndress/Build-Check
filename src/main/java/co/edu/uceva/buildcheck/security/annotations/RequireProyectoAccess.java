package co.edu.uceva.buildcheck.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar que el usuario tiene acceso a un proyecto específico
 * con un rol mínimo requerido
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireProyectoAccess {

    /**
     * El nombre del parámetro que contiene el proyectoId
     */
    String projectIdParam() default "id";

    /**
     * Roles permitidos para acceder (si está vacío, solo verifica que sea miembro)
     */
    String[] allowedRoles() default {};
}
