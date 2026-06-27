package com.nelumbo.citas_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nelumbo.citas_api.config.RsaKeysConfig;

@SpringBootApplication
// Habilita la carga de propiedades de claves RSA
@EnableConfigurationProperties(RsaKeysConfig.class)
public class CitasApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitasApiApplication.class, args);
	}

}
