package co.edu.uceva.buildcheck.security.aspects;

import co.edu.uceva.buildcheck.security.annotations.RequireProyectoAccess;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.service.IUsuarioProyectoService;
import co.edu.uceva.buildcheck.security.Jwt;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

@Aspect
@Component
public class ProyectoAccessAspect {

    @Autowired
    private IUsuarioProyectoService usuarioProyectoService;

    @Autowired
    private Jwt jwt;

    @Around("@annotation(co.edu.uceva.buildcheck.security.annotations.RequireProyectoAccess)")
    public Object validateProyectoAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // Obtener la anotación
        RequireProyectoAccess annotation = getAnnotation(joinPoint);

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OperacionNoPermitidaException("Usuario no autenticado");
        }

        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        // Obtener el proyectoId de los parámetros
        Long proyectoId = getProyectoIdFromArgs(joinPoint, annotation.projectIdParam());

        if (proyectoId == null) {
            throw new OperacionNoPermitidaException("ProyectoId no encontrado en los parámetros");
        }

        // Verificar que el usuario es miembro del proyecto
        boolean esMiembro = usuarioProyectoService.esUsuarioMiembroDelProyecto(usuarioId, proyectoId);
        if (!esMiembro) {
            throw new OperacionNoPermitidaException("No tienes acceso a este proyecto");
        }

        // Si hay roles especificados, verificar el rol
        if (annotation.allowedRoles().length > 0) {
            var usuarioProyecto = usuarioProyectoService.obtenerRolUsuarioEnProyecto(usuarioId, proyectoId);
            if (usuarioProyecto.isEmpty()) {
                throw new OperacionNoPermitidaException("No se pudo determinar tu rol en el proyecto");
            }

            String rolActual = usuarioProyecto.get().getRolProyecto().name();
            boolean tieneRol = Arrays.asList(annotation.allowedRoles()).contains(rolActual);

            if (!tieneRol) {
                throw new OperacionNoPermitidaException(
                        "No tienes permisos suficientes en este proyecto. Rol requerido: "
                                + Arrays.toString(annotation.allowedRoles()));
            }
        }

        return joinPoint.proceed();
    }

    private RequireProyectoAccess getAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();

            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    RequireProyectoAccess annotation = method.getAnnotation(RequireProyectoAccess.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        } catch (Exception e) {
            // Si algo falla, continuamos sin validación adicional
        }
        return null;
    }

    private Long getProyectoIdFromArgs(ProceedingJoinPoint joinPoint, String projectIdParam) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = getParameterNames(joinPoint);

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(projectIdParam) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }

        return null;
    }

    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();

            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    Parameter[] parameters = method.getParameters();
                    String[] names = new String[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        PathVariable pathVar = parameters[i].getAnnotation(PathVariable.class);
                        if (pathVar != null && !pathVar.value().isEmpty()) {
                            names[i] = pathVar.value();
                        } else {
                            names[i] = parameters[i].getName();
                        }
                    }

                    return names;
                }
            }
        } catch (Exception e) {
            // Si algo falla, retornamos un array vacío
        }

        return new String[0];
    }
}
