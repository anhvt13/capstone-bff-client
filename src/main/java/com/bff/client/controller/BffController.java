package com.bff.client.controller;

import com.bff.client.model.Driver;
import com.bff.client.model.ErrorResponse;
import com.bff.client.model.SliceDTO;
import com.bff.client.service.DriverService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class BffController {

    private static final Logger log = LoggerFactory.getLogger(BffController.class);

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    private DriverService driverService;

    @GetMapping ("/home")
    public String home() {
        return "Welcome Page of BFF-service";
    }

    @GetMapping("/browser-login-token")
    public ResponseEntity<?> getToken(Authentication authentication) {
        String principalName = authentication.getName();

        //TODO: AuthorizedClientService is created on the OAuth2 Client side.
        OAuth2AuthorizedClient client =  authorizedClientService.loadAuthorizedClient("cognito", principalName);
        if (client == null) {
            System.out.println("Unauthorized");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(LocalDateTime.now(), "Unauthorized"));
        }

/*        Token token = new Token (
                client.getClientRegistration().getRegistrationId(),
                client.getPrincipalName(),
                client.getAccessToken().getTokenType().getValue(),
                client.getAccessToken().getTokenValue(),
                client.getAccessToken().getIssuedAt().toString(),
                client.getAccessToken().getExpiresAt().toString());*/

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("OK");
    }

    @GetMapping("/auth-granted-token")
    public ResponseEntity<?> jwtInfo(JwtAuthenticationToken authentication) {

        //TODO: JwtAuthenticationToken is created by the Resource Server from the Bearer token.
        Jwt jwt = authentication.getToken();
  /*      Token token = new Token (
                "cognito",
                jwt.getSubject(),
                "Bearer",
                jwt.getTokenValue(),
                jwt.getIssuedAt().toString(),
                jwt.getExpiresAt().toString());*/

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jwt);
    }

    @GetMapping("/client-creds-token")
    public ResponseEntity<?> getClientToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                        .withClientRegistrationId("driver-service")
                        .principal("bff-client")
                        .build();

        String authManagerClass =  authorizedClientManager.getClass().getName();
        log.info("Current Authorized Client Manager: " + authManagerClass);

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            throw new IllegalStateException("Unable to obtain OAuth2 access token");
        }

        OAuth2AccessToken token = authorizedClient.getAccessToken();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(token);
    }

    @GetMapping("/token-claims")
    public Map<String, Object> tokenInfo(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }

    @GetMapping ("/driver/health")
    public ResponseEntity<Map<String, Object>> checkDriverServiceHealth() {
        log.info("Forwarding request to Driver micro-service for health checking");
        return driverService.checkServiceHealth();
    }

    @GetMapping("/drivers")
    public ResponseEntity<SliceDTO<Driver>> getDrivers(@RequestParam MultiValueMap<String, String> queryParams) {
        log.info("Forwarding request to Driver micro-service for retrieving list of drivers");
        return new ResponseEntity<>(driverService.getDrivers(queryParams), HttpStatus.OK);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Driver> getDriver(@PathVariable Integer driverId) {
        log.info("Forwarding request to Driver micro-service for retrieving individual driver");
        return new ResponseEntity<>(driverService.getDriver(driverId), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_openid')")
    @PostMapping("/driver")
    public ResponseEntity<Driver> saveDriver(@Valid @RequestBody Driver driver) {
        log.info("Forward request to Driver micro-service for inserting new individual driver");
        return new ResponseEntity<>(driverService.saveDriver(driver), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_openid')")
    @DeleteMapping("/driver/{driverId}")
    public ResponseEntity<Void> saveDriver(@PathVariable Integer driverId) {
        log.info("Forward request to Driver micro-service for deleting an existing driver");
        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }
    
}
