package com.autobots.automanager.controllers;

import com.autobots.automanager.entities.Company;
import com.autobots.automanager.entities.Merchandise;
import com.autobots.automanager.repositories.CompanyRepository;
import com.autobots.automanager.repositories.MerchandiseRepository;
import com.autobots.automanager.services.AddLinkMerchandise;
import com.autobots.automanager.services.UpdateMerchandise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/merchandise")
public class MerchandiseController {

  @Autowired
  private MerchandiseRepository merchandiseRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private AddLinkMerchandise addLink;

  @Autowired
  private UpdateMerchandise updateMerchandise;

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
  @GetMapping("/list")
  public ResponseEntity<List<Merchandise>> listMerchandise() {
    List<Merchandise> merchandise = merchandiseRepository.findAll();
    if (merchandise.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    addLink.addLink(new HashSet<>(merchandise));
    return ResponseEntity.ok(merchandise);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
  @GetMapping("/{id}")
  public ResponseEntity<Merchandise> getMerchandise(@PathVariable Long id) {
    Optional<Merchandise> merchandise = merchandiseRepository.findById(id);
    if (merchandise.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    addLink.addLink(merchandise.get());
    return ResponseEntity.ok(merchandise.get());
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @PostMapping("/create")
  public ResponseEntity<?> createMerchandise(@RequestBody Merchandise merchandise) {
    if (merchandise.getCompany() == null || merchandise.getCompany().getId() == null) {
      return ResponseEntity.badRequest().body("Company is required");
    }

    Optional<Company> companyOptional = companyRepository.findById(merchandise.getCompany().getId());
    if (companyOptional.isEmpty()) {
      return ResponseEntity.badRequest().body("Company not found");
    }

    merchandise.setCompany(companyOptional.get());

    Merchandise newMerchandise = merchandiseRepository.save(merchandise);
    addLink.addLink(newMerchandise);
    return ResponseEntity.ok(newMerchandise);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @PutMapping("/update/{id}")
  public ResponseEntity<Merchandise> updateMerchandise(@PathVariable Long id,
      @RequestBody Merchandise updatedMerchandise) {
    Optional<Merchandise> merchandiseOptional = merchandiseRepository.findById(id);
    if (merchandiseOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Merchandise merchandise = merchandiseOptional.get();
    updateMerchandise.update(merchandise, updatedMerchandise);
    Merchandise savedMerchandise = merchandiseRepository.save(merchandise);

    savedMerchandise.getCompany().getLegalName();

    addLink.addLink(savedMerchandise);
    return ResponseEntity.ok(savedMerchandise);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteMerchandise(@PathVariable Long id) {
    Optional<Merchandise> merchandise = merchandiseRepository.findById(id);
    if (merchandise.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    merchandiseRepository.delete(merchandise.get());
    return ResponseEntity.noContent().build();
  }
}
