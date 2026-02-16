package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoDAOMySQL implements TurnoDAO {

    // SELECT DETALLADO
    private static final String SELECT_DETALLADO = """
        SELECT
            t.id,
            t.medico_id,
            t.paciente_id,
            t.consultorio_id,
            t.fecha,
            t.hora,
            t.estado,
            t.observacion,

            m.nombre        AS medico_nombre,
            m.apellido      AS medico_apellido,
            m.especialidad  AS medico_especialidad,
            m.honorario     AS medico_honorario,
            um.username     AS medico_username,

            p.nombre        AS paciente_nombre,
            p.apellido      AS paciente_apellido,
            p.dni           AS paciente_dni,
            up.username     AS paciente_username,

            c.numero        AS consultorio_numero

        FROM turno t
        JOIN medico m        ON m.id = t.medico_id
        LEFT JOIN usuario um ON um.id = m.usuario_id

        JOIN paciente p      ON p.id = t.paciente_id
        LEFT JOIN usuario up ON up.id = p.usuario_id

        JOIN consultorio c   ON c.id = t.consultorio_id
        """;

    // CREATE
    @Override
    public int create(Turno t) throws DAOException {
        String sql = """
            INSERT INTO turno (medico_id, paciente_id, consultorio_id, fecha, hora, estado, observacion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getMedico().getId());
            ps.setInt(2, t.getPaciente().getId());
            ps.setInt(3, t.getConsultorio().getId());
            ps.setDate(4, Date.valueOf(t.getFecha()));
            ps.setTime(5, Time.valueOf(t.getHora()));
            ps.setString(6, t.getEstado().name());
            ps.setString(7, t.getObservacion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new DAOException("No se pudo obtener el ID generado del turno.");

        } catch (SQLException e) {
            throw new DAOException("Error creando turno", e);
        }
    }

    // UPDATE
    @Override
    public void update(Turno t) throws DAOException, NotFoundException {
        String sql = """
            UPDATE turno
            SET medico_id = ?, paciente_id = ?, consultorio_id = ?, fecha = ?, hora = ?, estado = ?, observacion = ?
            WHERE id = ?
            """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, t.getMedico().getId());
            ps.setInt(2, t.getPaciente().getId());
            ps.setInt(3, t.getConsultorio().getId());
            ps.setDate(4, Date.valueOf(t.getFecha()));
            ps.setTime(5, Time.valueOf(t.getHora()));
            ps.setString(6, t.getEstado().name());
            ps.setString(7, t.getObservacion());
            ps.setInt(8, t.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new NotFoundException("Turno no encontrado (ID " + t.getId() + ")");

        } catch (SQLException e) {
            throw new DAOException("Error actualizando turno", e);
        }
    }

    // DELETE
    @Override
    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM turno WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new NotFoundException("Turno no encontrado (ID " + id + ")");

        } catch (SQLException e) {
            throw new DAOException("Error eliminando turno", e);
        }
    }

    // FIND BY ID
    @Override
    public Turno findById(int id) throws DAOException, NotFoundException {
        String sql = SELECT_DETALLADO + " WHERE t.id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new NotFoundException("Turno no encontrado (ID " + id + ")");
                return mapRowDetallado(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error buscando turno por ID", e);
        }
    }

    // FIND ALL
    @Override
    public List<Turno> findAll() throws DAOException {
        String sql = SELECT_DETALLADO + " ORDER BY t.fecha, t.hora";
        List<Turno> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapRowDetallado(rs));
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error listando turnos", e);
        }
    }

    // EXISTS TURNO
    @Override
    public boolean existsTurnoMedicoFechaHora(int medicoId, LocalDate fecha, String hora) throws DAOException {
        String sql = """
            SELECT 1
            FROM turno
            WHERE medico_id = ? AND fecha = ? AND hora = ?
            LIMIT 1
            """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicoId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DAOException("Error verificando existencia de turno", e);
        }
    }

    // BETWEEN DATES
    @Override
    public List<Turno> findBetweenDates(LocalDate desde, LocalDate hasta) throws DAOException {
        String sql = SELECT_DETALLADO + """
            WHERE t.fecha BETWEEN ? AND ?
            ORDER BY t.fecha, t.hora
            """;

        List<Turno> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRowDetallado(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error obteniendo turnos entre fechas", e);
        }
    }

    // BY MEDICO BETWEEN DATES
    @Override
    public List<Turno> findByMedicoBetweenDates(int medicoId, LocalDate desde, LocalDate hasta) throws DAOException {
        String sql = SELECT_DETALLADO + """
            WHERE t.medico_id = ? AND t.fecha BETWEEN ? AND ?
            ORDER BY t.fecha, t.hora
            """;

        List<Turno> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicoId);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRowDetallado(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error obteniendo turnos por médico entre fechas", e);
        }
    }

    // MAPPER
    private Turno mapRowDetallado(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");

        Medico medico = new Medico();
        medico.setId(rs.getInt("medico_id"));
        medico.setNombre(rs.getString("medico_nombre"));
        medico.setApellido(rs.getString("medico_apellido"));
        medico.setEspecialidad(rs.getString("medico_especialidad"));
        medico.setHonorario(rs.getDouble("medico_honorario")); // <-- CLAVE para recaudación

        Paciente paciente = new Paciente();
        paciente.setId(rs.getInt("paciente_id"));
        paciente.setNombre(rs.getString("paciente_nombre"));
        paciente.setApellido(rs.getString("paciente_apellido"));
        paciente.setDni(rs.getString("paciente_dni"));

        Consultorio consultorio = new Consultorio();
        consultorio.setId(rs.getInt("consultorio_id"));
        consultorio.setNumero(rs.getString("consultorio_numero"));

        LocalDate fecha = rs.getDate("fecha").toLocalDate();
        LocalTime hora = rs.getTime("hora").toLocalTime();
        EstadoTurno estado = EstadoTurno.valueOf(rs.getString("estado"));
        String observacion = rs.getString("observacion");

        return new Turno(id, medico, paciente, consultorio, fecha, hora, estado, observacion);
    }
}
