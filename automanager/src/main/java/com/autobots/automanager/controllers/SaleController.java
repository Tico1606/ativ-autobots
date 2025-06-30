package com.autobots.automanager.controllers;

import com.autobots.automanager.entities.Company;
import com.autobots.automanager.entities.Sale;
import com.autobots.automanager.repositories.CompanyRepository;
import com.autobots.automanager.repositories.SaleRepository;
import com.autobots.automanager.services.AddLinkSale;
import com.autobots.automanager.services.UpdateSale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sale")
public class SaleController {

  @Autowired
  private SaleRepository saleRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private UpdateSale updateSale;

  @Autowired
  private AddLinkSale addLink;

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @GetMapping("/list")
  public ResponseEntity<List<Sale>> listSales() {
    List<Sale> sales = saleRepository.findAll();

    if (sales.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    addLink.addLink(new HashSet<>(sales));
    return ResponseEntity.ok(sales);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
  @PostMapping("/create")
  public ResponseEntity<?> createSale(@RequestBody Sale sale) {
    if (sale.getCompany() == null || sale.getCompany().getId() == null) {
      return ResponseEntity.badRequest().body("Company is required");
    }

    Optional<Company> companyOptional = companyRepository.findById(sale.getCompany().getId());
    if (companyOptional.isEmpty()) {
      return ResponseEntity.badRequest().body("Company not found");
    }

    sale.setCompany(companyOptional.get());

    Sale newSale = saleRepository.save(sale);
    return ResponseEntity.ok(newSale);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR', 'CLIENTE')")
  public ResponseEntity<Sale> getSale(@PathVariable Long id) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loggedUsername = auth.getName();

    boolean isCliente = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

    Optional<Sale> saleOpt = saleRepository.findById(id);
    if (saleOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Sale sale = saleOpt.get();

    if (isCliente) {
      if (!sale.getCustomer().getCredentialUserPassword().getUsername().equals(loggedUsername)) {
        return ResponseEntity.status(403).build();
      }
    }

    addLink.addLink(sale);
    return ResponseEntity.ok(sale);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @PutMapping("/update/{id}")
  public ResponseEntity<Sale> update(@PathVariable Long id, @RequestBody Sale updatedSale) {
    return saleRepository.findById(id)
        .map(sale -> {
          updateSale.update(sale, updatedSale);
          Sale savedSale = saleRepository.save(sale);
          savedSale.getCompany().getLegalName();
          addLink.addLink(savedSale);
          return ResponseEntity.ok(savedSale);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    Optional<Sale> saleOptional = saleRepository.findById(id);
    if (saleOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    saleRepository.delete(saleOptional.get());
    return ResponseEntity.noContent().build();
  }
}
