# Build-Check

## Reglas De Colaboración Segun El Modelo **GitFlow**

### 1. Estructura de Ramas
* **`main`**: Reservada exclusivamente para versiones estables y producción.
* **`develop`**: Rama principal de integración. Todo el código nuevo debe converger aquí.
* **`feature/`**: Ramas temporales creadas para desarrollar una funcionalidad específica (ejemplo: `feature/ocr-camera`). Nacen de `develop`.
* **`hotfix/`**: Ramas de emergencia para corregir errores críticos en la rama `main`.

### 2. Ciclo de Trabajo
Para cada nueva tarea asignada, seguir los siguientes pasos:

1.  **Sincronizar:** Asegúrate de tener la última versión de la rama de integración.
    ```bash
    git checkout develop
    git pull origin develop
    ```
2.  **Crear Rama:** Crea tu rama de trabajo con un nombre descriptivo.
    ```bash
    git checkout -b feature/nombre-de-tu-tarea
    ```
3.  **Desarrollar:** Realiza tus cambios y crea commits con frecuencia.
4.  **Subir Cambios:** Envía tu rama al repositorio remoto en GitHub.
    ```bash
    git push origin feature/nombre-de-tu-tarea
    ```
5. **Limpieza de Ramas:** Una vez que el Pull Request haya sido aprobado y fusionado (Merged) en GitHub, elimina la rama para mantener el repositorio limpio.

    **En tu PC (Local):**
    ```bash
    git checkout develop
    git pull origin develop
    git branch -d nombre-de-tu-tarea # Recuerda Hacer Referencia al nombre completo de la rama, (ejemplo: git branch -d feature/nombre-de-tu-tarea)
    ```

### 3. Pull Requests (PR) y Revisión
La integración de cualquier código a la rama `main` y `deve` se hará mediante Pull Requests en GitHub:
* **Revisión Obligatoria:** Al menos un compañero del equipo debe revisar el código y aprobar el PR.
* **Resolución de Conflictos:** Si existen conflictos el autor de la rama es responsable de resolverlos en su local antes de hacer el Merge.
* **Limpieza:** Una vez aceptada y fusionada la rama, debe ser eliminada para mantener el repositorio limpio.

### 4. Estándar de Commits
Se usaran los prefijos para identificar rápidamente de que se trata el cambio:
* `feat:` Nueva funcionalidad para el sistema.
* `fix:` Corrección de un error o bug.
* `docs:` Cambios solo en la documentación del proyecto.
* `refactor:` Mejora del código sin cambiar su funcionalidad.
* `style:` Cambios de formato (espacios, indentación) que no afectan la lógica.