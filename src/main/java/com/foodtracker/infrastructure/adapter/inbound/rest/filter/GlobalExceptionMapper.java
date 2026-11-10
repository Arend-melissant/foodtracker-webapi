package com.foodtracker.infrastructure.adapter.inbound.rest.filter;

import com.foodtracker.domain.exception.EntityNotFoundException;
import com.foodtracker.domain.exception.UnauthorizedException;
import com.foodtracker.domain.exception.ValidationException;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.ErrorResponseDto;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        int status;
        String title;

        if (exception instanceof EntityNotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            title = "Not Found";
        } else if (exception instanceof UnauthorizedException) {
            status = Response.Status.UNAUTHORIZED.getStatusCode();
            title = "Unauthorized";
        } else if (exception instanceof ValidationException || exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            title = "Bad Request";
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            title = "Internal Server Error";
            logger.error("Unhandled exception processing request: {}", exception.getMessage(), exception);
        }

        ErrorResponseDto errorDto = new ErrorResponseDto(
                title,
                status,
                exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred"
        );

        // Record exception and error status on current OpenTelemetry trace span if active
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
            currentSpan.recordException(exception);
            if (status >= 500) {
                currentSpan.setStatus(StatusCode.ERROR, errorDto.getDetail());
            }
        }

        MediaType mediaType = determineMediaType(headers);

        return Response.status(status)
                .type(mediaType)
                .entity(errorDto)
                .build();
    }

    private MediaType determineMediaType(HttpHeaders httpHeaders) {
        if (httpHeaders != null) {
            List<MediaType> acceptable = httpHeaders.getAcceptableMediaTypes();
            for (MediaType mt : acceptable) {
                if (mt.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
                    return MediaType.APPLICATION_XML_TYPE;
                }
                if (mt.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                    return MediaType.APPLICATION_JSON_TYPE;
                }
            }
        }
        return MediaType.APPLICATION_JSON_TYPE;
    }
}

