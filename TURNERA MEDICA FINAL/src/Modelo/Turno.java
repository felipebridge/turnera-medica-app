package Modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private int id;
    private Medico medico;
    private Paciente paciente;
    private Consultorio consultorio;

    private LocalDate fecha;
    private LocalTime hora;

    private EstadoTurno estado;
    private String observacion;

    public Turno() {}

    public Turno(Medico medico, Paciente paciente, Consultorio consultorio,
                 LocalDate fecha, LocalTime hora, EstadoTurno estado, String observacion) {
        this.medico = medico;
        this.paciente = paciente;
        this.consultorio = consultorio;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.observacion = observacion;
    }

    public Turno(int id, Medico medico, Paciente paciente, Consultorio consultorio,
                 LocalDate fecha, LocalTime hora, EstadoTurno estado, String observacion) {
        this.id = id;
        this.medico = medico;
        this.paciente = paciente;
        this.consultorio = consultorio;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.observacion = observacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Consultorio getConsultorio() { return consultorio; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    @Override
    public String toString() {
        return "Turno #" + id + " - " + fecha + " " + hora + " - " + estado;
    }
}
