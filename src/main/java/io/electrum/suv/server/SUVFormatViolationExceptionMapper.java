package io.electrum.suv.server;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.util.SUVModelUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.ArrayList;
import java.util.List;

import static io.electrum.suv.server.util.SUVModelUtils.buildFormatErrorRsp;

@Provider
public class SUVFormatViolationExceptionMapper implements ExceptionMapper<FormatException> {
    @Override
    public Response toResponse(FormatException exception) {

        List<String> errors = new ArrayList<>();
        errors.add(exception.getFormatError().getMsg());
        return Response.status(Response.Status.BAD_REQUEST).entity(buildFormatErrorRsp(errors)).build();

    }
}
