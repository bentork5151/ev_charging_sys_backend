package com.bentork.ev_system.exception;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

    // -----------------------------------------------------
    // SHORT LOGGING WITH TRY–CATCH & THROW
    // -----------------------------------------------------
    private void logError(Exception ex) {
        try {
            LOGGER.log(Level.SEVERE,
                    "ERROR: {0} → {1}", 
                    new Object[]{ex.getClass().getSimpleName(), ex.getMessage()});

        } catch (Exception logEx) {
            throw new RuntimeException("Logging failed: " + logEx.getMessage());
        }
    }

    // Response Builder
    private ErrorResponse build(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message);
    }


    // =====================================================
    // 400 ERRORS – BAD REQUEST
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        logError(ex);
        try {
            String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST, msg), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("Error handling validation");
        }
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST, "Binding error"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new RuntimeException("Bind handler failed");
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParam(MissingServletRequestParameterException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                    "Missing parameter: " + ex.getParameterName()), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("Missing param handler failed");
        }
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> missingPath(MissingPathVariableException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                    "Missing path variable: " + ex.getVariableName()), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("Missing path handler failed");
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                    "Malformed JSON request"), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("JSON handler failed");
        }
    }


    // =====================================================
    // 401 / 403 ERRORS – AUTH & SECURITY
    // =====================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"), HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            throw new RuntimeException("Bad credentials handler failed");
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.FORBIDDEN,
                    "Access denied"), HttpStatus.FORBIDDEN);

        } catch (Exception e) {
            throw new RuntimeException("Access denied handler failed");
        }
    }


    // =====================================================
    // 404 ERRORS – NOT FOUND
    // =====================================================

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> noHandler(NoHandlerFoundException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.NOT_FOUND,
                    "Invalid endpoint: " + ex.getRequestURL()), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            throw new RuntimeException("NoHandler handler failed");
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResource(NoResourceFoundException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.NOT_FOUND,
                    "Resource not found"), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            throw new RuntimeException("NoResource handler failed");
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(EntityNotFoundException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.NOT_FOUND,
                    ex.getMessage()), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            throw new RuntimeException("EntityNotFound handler failed");
        }
    }


    // =====================================================
    // 405 METHOD NOT ALLOWED
    // =====================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.METHOD_NOT_ALLOWED,
                    "HTTP Method Not Allowed"), HttpStatus.METHOD_NOT_ALLOWED);

        } catch (Exception e) {
            throw new RuntimeException("MethodNotAllowed handler failed");
        }
    }


    // =====================================================
    // DATABASE ERRORS
    // =====================================================

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> dataAccess(DataAccessException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database access error"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("DataAccess handler failed");
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrity(DataIntegrityViolationException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database constraint violation"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("DataIntegrity handler failed");
        }
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> sql(SQLException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "SQL error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("SQL handler failed");
        }
    }


    // =====================================================
    // IO + HARDWARE ERRORS
    // =====================================================

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> io(IOException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "I/O or Hardware error"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("IO handler failed");
        }
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> fileNotFound(FileNotFoundException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.NOT_FOUND,
                    "Requested file not found"), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            throw new RuntimeException("FileNotFound handler failed");
        }
    }

    @ExceptionHandler(EOFException.class)
    public ResponseEntity<ErrorResponse> eof(EOFException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                    "Unexpected end of input"), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("EOF handler failed");
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> maxUpload(MaxUploadSizeExceededException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size exceeds maximum limit"), HttpStatus.PAYLOAD_TOO_LARGE);

        } catch (Exception e) {
            throw new RuntimeException("MaxUpload handler failed");
        }
    }


    // =====================================================
    // JAVA RUNTIME ERRORS
    // =====================================================

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> nullPointer(NullPointerException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Null encountered in request"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("NullPointer handler failed");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgument(IllegalArgumentException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                    ex.getMessage()), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            throw new RuntimeException("IllegalArgument handler failed");
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalState(IllegalStateException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("IllegalState handler failed");
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtime(RuntimeException ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Runtime error"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("Runtime handler failed");
        }
    }


    // =====================================================
    // FINAL ROOT EXCEPTION HANDLER
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex) {
        logError(ex);
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("General exception handler failed");
        }
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> root(Throwable ex) {
        logError(new Exception(ex));
        try {
            return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected system-level error"), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException("Root handler failed");
        }
    }
}
