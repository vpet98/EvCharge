package org.killercarrots.evcharge.errorHandling;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class Handler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    protected ResponseEntity<String> MultipartHandler(MultipartException e) {
        ApiError err = new ApiError(400, "Bad request", e.getMessage());
        return err.buildErrorResponse(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<String> BadCRedentialsHandler(BadCredentialsException e) {
        ApiError err = new ApiError(400, "Bad request", e.getMessage());
        return err.buildErrorResponse(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<String> BadRequestHandler(BadRequestException e) {
        ApiError err = new ApiError(400, "Bad request", e.getMessage());
        return err.buildErrorResponse(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    protected ResponseEntity<String> NotAuthorized(NotAuthorizedException e) {
        ApiError err = new ApiError(401, "Not authorized", e.getMessage());
        return err.buildErrorResponse(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<String> AccessDenied(AccessDeniedException e) {
        ApiError err = new ApiError(401, "Not authorized", e.getMessage());
        return err.buildErrorResponse(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoDataException.class)
    protected ResponseEntity<String>  NoDataHandler(NoDataException e) {
        ApiError err = new ApiError(402, "No data", e.getMessage());
        // 402 status code somehow refers to Payment Required
        return err.buildErrorResponse(HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<String> DBDown(DataAccessException e) {
        ApiError err = new ApiError(503, "Service unavailable", e.getMessage());
        return err.buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> generalHandlerHandler(Exception e) {
        ApiError err = new ApiError(500, "Unexpected error occured", e.getMessage());
        return err.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
