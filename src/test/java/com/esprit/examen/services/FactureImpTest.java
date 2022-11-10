package com.esprit.examen.services;

import static org.junit.Assert.*;

import com.esprit.examen.entities.Facture;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class FactureImpTest {
    @Autowired
    public IFactureService factureService ;
    @Test
    public void testFactureScenario() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateCreationFacture = dateFormat.parse("30/09/2000");
        Date dateDerniereModificationFacture = dateFormat.parse("30/09/2022");
        Facture facture = new Facture();
        facture.setMontantRemise(20f);
        facture.setMontantFacture(100f);
        facture.setDateCreationFacture(dateCreationFacture);
        facture.setDateDerniereModificationFacture(dateDerniereModificationFacture);
        facture.setArchivee(false);
        Facture factureadded =factureService.addFacture(facture);
        assertNotNull(factureadded.getIdFacture());
        assertFalse(facture.getArchivee());
        List<Facture> allfactures = factureService.retrieveAllFactures();
        allfactures.forEach(elem -> log.info(elem.toString()));
        factureService.cancelFacture(factureadded.getIdFacture());


    }




}
