# Turnera Médica – Trabajo Práctico Final

Sistema de gestión de turnos médicos desarrollado como Trabajo Práctico Final de la materia Laboratorio 1 (Programación 3) - Programación Orientada a Objetos.

El proyecto permite administrar médicos, pacientes, turnos, consultorios, usuarios y obras sociales, aplicando los conceptos de Programación Orientada a Objetos, arquitectura en capas, manejo de excepciones y persistencia en base de datos.

---

## Tecnologías utilizadas
- Java
- Swing (interfaz gráfica)
- MySQL
- JDBC
- JCalendar / JDateChooser / FlatLaf

---

## Entidades del sistema
El sistema está compuesto por las siguientes entidades:
- Médico
- Paciente
- Turno
- Consultorio
- Usuario
- Obra Social

El proyecto cumple con la restricción de no exceder las 6 entidades totales.

---

## Funcionalidades implementadas

### Funcionalidad básica (obligatoria)
- CRUD completo de Médicos, Pacientes, Turnos, Consultorios, Usuarios y Obras Sociales.
- Administración de turnos con fecha y hora.
- Restricción de negocio: no se permite asignar más de un turno al mismo médico en la misma fecha y hora.
- Selección de médico y paciente desde listas.
- Manejo de excepciones y validaciones.
- Interfaces gráficas desarrolladas con Swing.
- Persistencia de datos mediante JDBC.

### Reportes
- Reporte de turnos entre dos fechas.
- Reporte de turnos por médico entre dos fechas.
- Cálculo de recaudación y cantidad de consultas realizadas en el período seleccionado.
- La recaudación se calcula únicamente sobre los turnos con estado **ATENDIDO**.

### Funcionalidades adicionales
- Administración de consultorios como lugar físico de atención.
- Asociación de turnos a un consultorio específico.
- Manejo de usuarios para médicos y pacientes.
- Visualización de turnos mediante calendario.

---

## Arquitectura
El proyecto está organizado en capas:
- **UI**: Interfaces gráficas en Swing.
- **Service**: Lógica de negocio y validaciones.
- **DAO**: Acceso a datos y operaciones JDBC.
- **Modelo**: Entidades del dominio.
- **Exceptions**: Manejo centralizado de excepciones.
- **Config**: Configuración de conexión a la base de datos.

Se prioriza el bajo acoplamiento, alta cohesión y reutilización de código.

---

## Ejecución del proyecto
1. Crear la base de datos ejecutando el script SQL incluido.
2. Configurar los datos de conexión MySQL en la clase `ConexionMySQL`.
3. Importar el proyecto en el IDE (IntelliJ IDEA).
4. Ejecutar la clase principal del sistema.

---

## Observaciones
- El sistema valida las reglas de negocio antes de realizar operaciones en la base de datos.
- El diseño está orientado a objetos y separado en capas según lo visto en la materia.
- El proyecto fue desarrollado para ser defendido en instancia oral, justificando las decisiones de diseño adoptadas.
