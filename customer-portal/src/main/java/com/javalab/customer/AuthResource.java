package com.javalab.customer;

import jakarta.json.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.sql.*;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(InputStream input) throws SQLException {
        JsonObject request;
        try (JsonReader reader = Json.createReader(input)) {
            JsonStructure structure = reader.read();
            if (!(structure instanceof JsonObject jsonObject)) {
                throw new BadRequestException("Invalid request body");
            }
            request = jsonObject;
        } catch (JsonException | IllegalStateException error) {
            throw new BadRequestException("Invalid request body");
        }

        String username = request.containsKey("username") && !request.isNull("username") ? request.getString("username", "") : "";
        String password = request.containsKey("password") && !request.isNull("password") ? request.getString("password", "") : "";
        if (username.isBlank() || password.isBlank()) {
            throw new BadRequestException("Username and password are required");
        }

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, full_name FROM users WHERE username=? AND password_hash=? AND role='CUSTOMER' AND active=TRUE")) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("message", "Sai tài khoản hoặc mật khẩu."))
                            .build();
                }

                return Response.ok(Map.of(
                        "id", rs.getLong("id"),
                        "username", rs.getString("username"),
                        "fullName", rs.getString("full_name")
                )).build();
            }
        }
    }
}
