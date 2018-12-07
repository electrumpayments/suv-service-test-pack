package io.electrum.suv.server;

import static io.electrum.suv.server.util.SUVModelUtils.buildFormatErrorRsp;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import io.electrum.suv.server.model.FormatException;

/**
 * Custom exception mapper for {@link FormatException FormatExceptions}. Allows for uniform handling of custom format
 * errors alongside those handled by Hibernate Validation.
 */
@Provider
public class SUVFormatViolationExceptionMapper implements ExceptionMapper<FormatException> {
   
   @Override
   public Response toResponse(FormatException exception) {
      List<String> errors = new ArrayList<>();
      errors.add(exception.getFormatError().getMsg());
      return Response.status(Response.Status.BAD_REQUEST).entity(buildFormatErrorRsp(errors)).build();
   }
}
