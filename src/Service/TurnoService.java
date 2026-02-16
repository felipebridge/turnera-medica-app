package Service;

import DAO.TurnoDAO;
import DAO.TurnoDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.EstadoTurno;
import Modelo.Turno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TurnoService {

    private final TurnoDAO turnoDAO;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TurnoService() {
        this.turnoDAO = new TurnoDAOMySQL();
    }

    public int crear(Turno t) throws ValidationException, DAOException {
        validar(t);

        String horaStr = formatearHora(t.getHora());

        boolean existe = turnoDAO.existsTurnoMedicoFechaHora(
                t.getMedico().getId(),
                t.getFecha(),
                horaStr
        );

        if (existe) {
            throw new ValidationException("Ya existe un turno para ese médico en esa fecha y hora.");
        }

        return turnoDAO.create(t);
    }

    public void actualizar(Turno t) throws ValidationException, DAOException, NotFoundException {
        if (t.getId() <= 0) throw new ValidationException("ID inválido.");
        validar(t);
        turnoDAO.update(t);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        turnoDAO.delete(id);
    }

    public void cancelar(int turnoId) throws ValidationException, DAOException, NotFoundException {
        if (turnoId <= 0) throw new ValidationException("ID inválido.");
        Turno t = turnoDAO.findById(turnoId);
        t.setEstado(EstadoTurno.CANCELADO);
        turnoDAO.update(t);
    }

    public Turno buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        return turnoDAO.findById(id);
    }

    public List<Turno> listarEntreFechas(LocalDate desde, LocalDate hasta)
            throws ValidationException, DAOException {

        if (desde == null || hasta == null)
            throw new ValidationException("Las fechas no pueden ser nulas.");

        if (desde.isAfter(hasta))
            throw new ValidationException("La fecha 'desde' no puede ser mayor a 'hasta'.");

        return turnoDAO.findBetweenDates(desde, hasta);
    }


    public List<Turno> listar() throws DAOException {
        return turnoDAO.findAll();
    }

    public List<Turno> reporteEntreFechas(LocalDate desde, LocalDate hasta)
            throws ValidationException, DAOException {
        validarRango(desde, hasta);
        return turnoDAO.findBetweenDates(desde, hasta);
    }

    public List<Turno> reporteMedicoEntreFechas(int medicoId, LocalDate desde, LocalDate hasta)
            throws ValidationException, DAOException {
        if (medicoId <= 0) throw new ValidationException("Médico inválido.");
        validarRango(desde, hasta);
        return turnoDAO.findByMedicoBetweenDates(medicoId, desde, hasta);
    }

    // Validaciones

    private void validar(Turno t) throws ValidationException {
        if (t == null) throw new ValidationException("Turno no puede ser null.");

        if (t.getMedico() == null || t.getMedico().getId() <= 0)
            throw new ValidationException("Médico obligatorio.");

        if (t.getPaciente() == null || t.getPaciente().getId() <= 0)
            throw new ValidationException("Paciente obligatorio.");

        if (t.getConsultorio() == null || t.getConsultorio().getId() <= 0)
            throw new ValidationException("Consultorio obligatorio.");

        if (t.getFecha() == null)
            throw new ValidationException("Fecha obligatoria.");

        if (t.getHora() == null)
            throw new ValidationException("Hora obligatoria.");

        if (t.getEstado() == null)
            t.setEstado(EstadoTurno.PENDIENTE);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) throws ValidationException {
        if (desde == null || hasta == null)
            throw new ValidationException("Fechas obligatorias.");
        if (hasta.isBefore(desde))
            throw new ValidationException("La fecha 'hasta' no puede ser menor que 'desde'.");
    }

    private String formatearHora(LocalTime hora) {
        return hora.format(TIME_FMT); // "HH:mm:ss"
    }
}

