package com.atlas.bank.shared.exception;

import com.atlas.bank.account.exception.AccountNotFoundException;
import com.atlas.bank.transaction.exception.AccountNotActiveException;
import com.atlas.bank.transaction.exception.InsufficientFundsException;
import com.atlas.bank.transaction.service.exception.FraudCheckException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler( AccountNotFoundException.class )
    public ProblemDetail handleAccountNotFoundException( AccountNotFoundException exception ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, exception.getMessage() );
        problem.setTitle( "Cuenta no encontrada" );

        return problem;

    }

    @ExceptionHandler( InsufficientFundsException.class )
    public ProblemDetail handleAInsufficientFundsException( InsufficientFundsException exception ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf( 422 ), exception.getMessage() );
        problem.setTitle( "Fondos insuficientes para realizar esta operacion" );

        return problem;

    }

    @ExceptionHandler( AccountNotActiveException.class )
    public ProblemDetail handleAccountNotActiveException( AccountNotActiveException exception ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf( 422 ), exception.getMessage() );
        problem.setTitle( "Cuenta no activa" );

        return problem;

    }

    @ExceptionHandler( MethodArgumentNotValidException.class )
    public ProblemDetail handleMethodArgumentNotValidException( MethodArgumentNotValidException exception ) {

        ProblemDetail problem = ProblemDetail.forStatus( HttpStatus.BAD_REQUEST );
        problem.setTitle( "Error de validación" );
        problem.setProperty( "timestamp", OffsetDateTime.now( ZoneOffset.UTC ) );

        List< ValidationError > errors = new ArrayList<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach( error -> errors.add(
                        new ValidationError( error.getField(), error.getDefaultMessage() )  ) );

        exception
                .getBindingResult()
                .getGlobalErrors()
                .forEach( error -> errors.add(
                        new ValidationError( "global", error.getDefaultMessage() ) ) );

        problem.setProperty( "errors", errors );

        return problem;

    }

    @ExceptionHandler( FraudCheckException.class )
    public ProblemDetail handleFraudCheckException( FraudCheckException exception ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf( 422 ), exception.getMessage() );

        problem.setTitle( "Operación bloqueada por fraude" );
        problem.setProperty( "timestamp", OffsetDateTime.now( ZoneOffset.UTC ) );

        return problem;

    }

    @ExceptionHandler( Exception.class )
    public ProblemDetail handleException( Exception exception ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno en el sistema" );

        problem.setTitle( "Error interno en el sistema" );

        log.error( "Error inesperado: ", exception );

        return problem;

    }

}