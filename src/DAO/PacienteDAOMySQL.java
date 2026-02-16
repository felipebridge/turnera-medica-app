package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.ObraSocial;
import Modelo.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAOMySQL implements PacienteDAO {

    @Override
    public int create(Paciente p) throws DAOException {
        String sql = """
            INSERT INTO paciente (nombre, apellido, dni, telefono, email, obra_social_id, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getDni());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());

            if (p.getObraSocial() != null) ps.setInt(6, p.getObraSocial().getId());
            else ps.setNull(6, Types.INTEGER);

            ps.setBoolean(7, p.isActivo());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            throw new DAOException("No se generó ID para el paciente");

        } catch (SQLException e) {
            throw new DAOException("Error creando paciente", e);
        }
    }

    @Override
    public void update(Paciente p) throws DAOException {
        String sql = """
            UPDATE paciente
            SET nombre=?, apellido=?, dni=?, telefono=?, email=?, obra_social_id=?, activo=?
            WHERE id=?
        """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getDni());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());

            if (p.getObraSocial() != null) ps.setInt(6, p.getObraSocial().getId());
            else ps.setNull(6, Types.INTEGER);

            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new DAOException("No se actualizó paciente (id inexistente)");

        } catch (SQLException e) {
            throw new DAOException("Error actualizando paciente", e);
        }
    }

    @Override
    public void delete(int id) throws DAOException {
        String sql = "DELETE FROM paciente WHERE id=?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Error eliminando paciente", e);
        }
    }

    @Override
    public Paciente findById(int id) throws DAOException, NotFoundException {
        String sql = """
            SELECT p.*, os.id AS os_id, os.nombre AS os_nombre, os.descuento
            FROM paciente p
            LEFT JOIN obra_social os ON os.id = p.obra_social_id
            WHERE p.id=?
        """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new NotFoundException("Paciente no encontrado (id=" + id + ")");

                ObraSocial os = rs.getInt("os_id") > 0
                        ? new ObraSocial(
                        rs.getInt("os_id"),
                        rs.getString("os_nombre"),
                        rs.getDouble("descuento")
                )
                        : null;

                return new Paciente(
                        rs.getInt("id"),
                        rs.getBoolean("activo"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        os
                );
            }

        } catch (SQLException e) {
            throw new DAOException("Error buscando paciente", e);
        }
    }

    @Override
    public List<Paciente> findAll() throws DAOException {
        List<Paciente> lista = new ArrayList<>();

        String sql = """
            SELECT p.*, os.id AS os_id, os.nombre AS os_nombre, os.descuento
            FROM paciente p
            LEFT JOIN obra_social os ON os.id = p.obra_social_id
            ORDER BY p.apellido, p.nombre
        """;

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ObraSocial os = rs.getInt("os_id") > 0
                        ? new ObraSocial(
                        rs.getInt("os_id"),
                        rs.getString("os_nombre"),
                        rs.getDouble("descuento")
                )
                        : null;

                lista.add(new Paciente(
                        rs.getInt("id"),
                        rs.getBoolean("activo"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        os
                ));
            }

        } catch (SQLException e) {
            throw new DAOException("Error listando pacientes", e);
        }

        return lista;
    }
}
