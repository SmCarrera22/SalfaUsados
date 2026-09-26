package cl.salfa.bff.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;

public class EntraTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final String requiredScope;

    public EntraTokenValidator(String requiredScope) {
        this.requiredScope = requiredScope;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String scopes = jwt.getClaimAsString("scp");

        boolean containsRequiredScope =
                scopes != null
                        && Arrays.asList(scopes.split(" "))
                        .contains(requiredScope);

        if (!containsRequiredScope) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Required scope is missing",
                    null
            );

            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}