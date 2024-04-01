package com.walmart.realestate.crystal.storereview;

import com.walmart.core.realestate.cerberus.advice.CerberusControllerAdvice;
import com.walmart.core.realestate.cerberus.controller.CerberusAuthenticationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.walmart.realestate.crystal",
        exclude = {
                CerberusAuthenticationController.class,
                CerberusControllerAdvice.class
        }
)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
