package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.SampleListToSampleId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

@Service
public class SampleListServiceImpl implements SampleListService {

  @Autowired private SampleListRepository sampleListRepository;
  @Autowired private StudyService studyService;

  @Value("${authenticate:false}")
  private String AUTHENTICATE;

  @Override
  @PostFilter(
      "hasPermission(filterObject, T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public List<SampleList> getAllSampleLists(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

    List<SampleList> sampleListsFromRepo =
        sampleListRepository.getAllSampleLists(projection, pageSize, pageNumber, sortBy, direction);
    // copy the list before returning so @PostFilter doesn't taint the list stored in the
    // persistence layer cache
    List<SampleList> sampleLists =
        (AUTHENTICATE.equals("false"))
            ? sampleListsFromRepo
            : new ArrayList<SampleList>(sampleListsFromRepo);

    if (projection.equals("DETAILED")) {
      addSampleIds(sampleLists);
      addSampleCounts(sampleLists);
    }

    return sampleLists;
  }

  @Override
  public BaseMeta getMetaSampleLists() {

    return sampleListRepository.getMetaSampleLists();
  }

  @Override
  public SampleList getSampleList(String sampleListId) throws SampleListNotFoundException {

    SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
    if (sampleList == null) {
      throw new SampleListNotFoundException(sampleListId);
    }

    List<SampleListToSampleId> sampleListToSampleIds =
        sampleListRepository.getSampleListSampleIds(Arrays.asList(sampleList.getListId()));
    sampleList.setSampleIds(
        sampleListToSampleIds.stream()
            .map(SampleListToSampleId::getSampleId)
            .collect(Collectors.toList()));
    sampleList.setSampleCount(sampleList.getSampleIds().size());
    return sampleList;
  }

  @Override
  public List<SampleList> getAllSampleListsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.getStudy(studyId);

    List<SampleList> sampleLists =
        sampleListRepository.getAllSampleListsInStudies(
            Arrays.asList(studyId), projection, pageSize, pageNumber, sortBy, direction);

    if (projection.equals("DETAILED")) {
      addSampleIds(sampleLists);
      addSampleCounts(sampleLists);
    }

    return sampleLists;
  }

  @Override
  public BaseMeta getMetaSampleListsInStudy(String studyId) throws StudyNotFoundException {

    studyService.getStudy(studyId);

    return sampleListRepository.getMetaSampleListsInStudy(studyId);
  }

  @Override
  public List<String> getAllSampleIdsInSampleList(String sampleListId)
      throws SampleListNotFoundException {

    getSampleList(sampleListId);

    return sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
  }

  @Override
  public List<SampleList> fetchSampleLists(List<String> sampleListIds, String projection) {

    List<SampleList> sampleLists = sampleListRepository.getSampleLists(sampleListIds, projection);

    if (projection.equals("DETAILED")) {
      addSampleIds(sampleLists);
      addSampleCounts(sampleLists);
    }

    return sampleLists;
  }

  private void addSampleCounts(List<SampleList> sampleLists) {

    sampleLists.forEach(s -> s.setSampleCount(s.getSampleIds().size()));
  }

  private void addSampleIds(List<SampleList> sampleLists) {

    for (SampleList sampleList : sampleLists) {
      sampleList.setSampleIds(
          sampleListRepository
              .getSampleListSampleIds(Arrays.asList(sampleList.getListId()))
              .stream()
              .map(SampleListToSampleId::getSampleId)
              .collect(Collectors.toList()));
    }
  }

  @Override
  public List<SampleList> getAllSampleListsInStudies(List<String> studyIds, String projection) {

    List<SampleList> sampleLists =
        sampleListRepository.getAllSampleListsInStudies(
            studyIds, projection, null, null, null, null);

    if (projection.equals("DETAILED")) {
      addSampleIds(sampleLists);
      addSampleCounts(sampleLists);
    }

    return sampleLists;
  }
}
