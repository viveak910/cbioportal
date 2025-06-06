package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.GenesetHierarchyInfo;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;

public interface GenesetHierarchyService {

  List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
      String geneticProfileId, Integer percentile, Double scoreThreshold, Double pvalueThreshold)
      throws MolecularProfileNotFoundException;

  List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
      String geneticProfileId,
      Integer percentile,
      Double scoreThreshold,
      Double pvalueThreshold,
      List<String> sampleIds)
      throws MolecularProfileNotFoundException;

  List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
      String geneticProfileId,
      Integer percentile,
      Double scoreThreshold,
      Double pvalueThreshold,
      String sampleListId)
      throws MolecularProfileNotFoundException, SampleListNotFoundException;
}
