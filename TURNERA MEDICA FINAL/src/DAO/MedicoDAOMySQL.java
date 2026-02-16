package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Medico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAOMySQL implements MedicoDAO {

    @Override
    public int create(Medico m) throws DAOException {
        String sql = """
                INSERT INTO medico (nombre, apellido, matricula, especialidad, honorario, activo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNombre());
            ps.setString(2, m.getApellido());
            ps.setString(3, m.getMatricula());
            ps.setString(4, m.getEspecialidad());
            ps.setDouble(5, m.getHonorario());
            ps.setBoolean(6, m.isActivo());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            throw new DAOException("No se pudo obtener el ID generado del médico.");

        } catch (SQLException e) {
            throw new DAOException("Error creando médico", e);
        }
    }

    @Override
    public void update(Medico m) throws DAOException, NotFoundException {
        String sql = """
                UPDATE medico
                SET nombre = ?, apellido = ?, matricula = ?, especialidad = ?, honorario = ?, activo = ?
                WHERE id = ?
                """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNombre());
            ps.setString(2, m.getApellido());
            ps.setString(3, m.getMatricula());
            ps.setString(4, m.getEspecialidad());
            ps.setDouble(5, m.getHonorario());
            ps.setBoolean(6, m.isActivo());
            ps.setInt(7, m.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new NotFoundException("Médico no encontrado (ID " + m.getId() + ")");
            }

        } catch (SQLException e) {
            throw new DAOException("Error actualizando médico", e);
        }
    }

    @Override
    public void delete(int id) throws DAOException, NotFoundException {
        String sqlTiene = "SELECT 1 FROM medico WHERE id = ?";
        String sqlBorrarTurnos = "DELETE FROM turno WHERE medico_id = ?";
        String sqlBorrarMedico = "DELETE FROM medico WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection()) {
            conn.setAutoCommit(false);

            // Verifica existencia
            try (PreparedStatement ps = conn.prepareStatement(sqlTiene)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new NotFoundException("Médico no encontrado (ID " + id + ")");
                    }
                }
            }

            // Borra turnos asociados
            try (PreparedStatement ps = conn.prepareStatement(sqlBorrarTurnos)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Borra médico
            int filas;
            try (PreparedStatement ps = conn.prepareStatement(sqlBorrarMedico)) {
                ps.setInt(1, id);
                filas = ps.executeUpdate();
            }

            if (filas == 0) {
                conn.rollback();
                throw new NotFoundException("Médico no encontrado (ID " + id + ")");
            }

            conn.commit();

        } catch (SQLException e) {
            throw new DAOException("Error eliminando médico", e);
        }
    }



    @Override
    public Medico findById(int id) throws DAOException, NotFoundException {
        String sql = """
                SELECT id, nombre, apellido, matricula, especialidad, honorario, activo
                FROM medico
                WHERE id = ?
                """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NotFoundException("Médico no encontrado (ID " + id + ")");
                }

                return new Medico(
                        rs.getInt("id"),
                        rs.getBoolean("activo"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("matricula"),
                        rs.getString("especialidad"),
                        rs.getDouble("honorario")
                );
            }

        } catch (SQLException e) {
            throw new DAOException("Error buscando médico", e);
        }
    }

    @Override
    public List<Medico> findAll() throws DAOException {
        List<Medico> lista = new ArrayList<>();

        String sql = """
                SELECT id, nombre, apellido, matricula, especialidad, honorario, activo
                FROM medico
                ORDER BY apellido, nombre
                """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Medico(
                        rs.getInt("id"),
                        rs.getBoolean("activo"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("matricula"),
                        rs.getString("especialidad"),
                        rs.getDouble("honorario")
                ));
            }

            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error listando médicos", e);
        }
    }
}
