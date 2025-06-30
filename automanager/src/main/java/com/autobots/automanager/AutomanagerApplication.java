package com.autobots.automanager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.autobots.automanager.entities.*;
import com.autobots.automanager.enums.DocumentType;
import com.autobots.automanager.enums.UserRole;
import com.autobots.automanager.repositories.*;

@SpringBootApplication
public class AutomanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomanagerApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedDatabase(
			CompanyRepository companyRepository,
			UserRepository userRepository,
			BCryptPasswordEncoder encoder) {
		return args -> {
			if (companyRepository.count() == 0 && userRepository.count() == 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				Address enderecoEmpresa = new Address();
				enderecoEmpresa.setState("SP");
				enderecoEmpresa.setCity("São Paulo");
				enderecoEmpresa.setDistrict("Jardim Paulista");
				enderecoEmpresa.setStreet("Rua das Flores");
				enderecoEmpresa.setNumber("123");
				enderecoEmpresa.setPostalCode("04101-300");
				enderecoEmpresa.setAdditionalInfo("Apto 101");

				Phone telefoneCompany = new Phone();
				telefoneCompany.setAreaCode("11");
				telefoneCompany.setNumber("99999-9999");

				Company empresa = new Company();
				empresa.setLegalName("Empresa 1");
				empresa.setTradeName("Empresa 1");
				empresa.setRegistrationDate(LocalDate.parse("2025-01-01"));
				empresa.setAddress(enderecoEmpresa);
				empresa.setPhones(new HashSet<>(Set.of(telefoneCompany)));

				empresa = companyRepository.save(empresa);

				Address enderecoUser = new Address();
				enderecoUser.setState("SP");
				enderecoUser.setCity("São Paulo");
				enderecoUser.setDistrict("Jardim dos Crias");
				enderecoUser.setStreet("Rua dos Noias");
				enderecoUser.setNumber("456");
				enderecoUser.setPostalCode("04101-300");
				enderecoUser.setAdditionalInfo("Apto 202");

				User admin = new User();
				admin.setName("João da Silva");
				admin.setSocialName("João da Silva");
				admin.setRole(UserRole.ADMIN);
				admin.setInactive(false);
				admin.setAddress(enderecoUser);

				Document documento = new Document();
				documento.setType(DocumentType.CPF);
				documento.setIssueDate(sdf.parse("2020-01-01"));
				documento.setNumber("0124567891");
				admin.setDocuments(new HashSet<>(Set.of(documento)));

				Phone telefoneUser = new Phone();
				telefoneUser.setAreaCode("11");
				telefoneUser.setNumber("99999-9999");
				admin.setPhones(new HashSet<>(Set.of(telefoneUser)));

				Email emailUser = new Email();
				emailUser.setEmail("admin@admin.com");
				admin.setEmails(new HashSet<>(Set.of(emailUser)));

				CredentialUserPassword credencial = new CredentialUserPassword();
				credencial.setUsername("admin@admin.com");
				credencial.setPassword(encoder.encode("admin123"));
				admin.setCredentialUserPassword(credencial);

				admin.setCompany(empresa);

				userRepository.save(admin);
			}
		};
	}
}
