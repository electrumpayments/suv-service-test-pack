package io.electrum.suv.server;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.electrum.suv.server.util.SUVModelUtils;
import org.glassfish.jersey.server.model.Invocable;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SUVViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
   @Override
   public Response toResponse(JerseyViolationException exception) {
      final Invocable invocable = exception.getInvocable();
      final List<String> errors =
            exception.getConstraintViolations()
                  .stream()
                  .map(violation -> ConstraintMessage.getMessage(violation, invocable))
                  .collect(Collectors.toList());

      return Response.status(Response.Status.BAD_REQUEST).entity(SUVModelUtils.buildFormatErrorRsp(errors)).build();
   }

//   JerseyViolationException e = new JerseyViolationException(new HashSet<ConstraintViolationException>(), Invocable.create())
}