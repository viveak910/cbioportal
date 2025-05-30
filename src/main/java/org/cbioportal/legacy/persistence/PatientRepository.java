package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface PatientRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Patient> getAllPatients(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaPatients(String keyword);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Patient> getAllPatientsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaPatientsInStudy(String studyId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  Patient getPatientInStudy(String studyId, String patientId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds);
}
