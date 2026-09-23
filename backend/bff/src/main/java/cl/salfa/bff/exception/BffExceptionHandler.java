package cl.salfa.bff.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleRestClientResponseException(
            RestClientResponseException exception
    ) {

        HttpHeaders headers = new HttpHeaders();

        if (exception.getResponseHeaders() != null
                && exception.getResponseHeaders().getContentType() != null) {

            headers.setContentType(
                    exception.getResponseHeaders().getContentType()
            );
        }

        return ResponseEntity
                .status(exception.getStatusCode())
                .headers(headers)
                .body(exception.getResponseBodyAsString());
    }
}