package io.electrum.suv.server;

import static io.electrum.suv.server.util.SUVModelUtils.buildFormatErrorRsp;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.electrum.suv.server.model.FormatException;

/**
 * Custom exception mapper for {@link com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
 * UnrecognizedPropertyException}. Catches errors caused by requests with unexpected fields present and returns a
 * sensible error response. Allows for uniform handling of custom format errors alongside those handled by Hibernate
 * Validation.
 */
@Provider
public class SUVUnrecognizedFieldViolationExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

   @Override
   public Response toResponse(UnrecognizedPropertyException exception) {
      List<String> errors = new ArrayList<>();
      errors.add("Unrecognized field '"+exception.getPropertyName()+"'");
      return Response.status(Response.Status.BAD_REQUEST).entity(buildFormatErrorRsp(errors)).build();
   }
}
