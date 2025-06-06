package org.cbioportal.legacy.web.util;

import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.service.ClinicalDataService;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinicalDataIntervalFilterApplier extends ClinicalDataFilterApplier {

  @Autowired
  public ClinicalDataIntervalFilterApplier(
      PatientService patientService,
      ClinicalDataService clinicalDataService,
      StudyViewFilterUtil studyViewFilterUtil) {
    super(patientService, clinicalDataService, studyViewFilterUtil);
  }

  @Override
  public Integer apply(
      List<ClinicalDataFilter> attributes,
      MultiKeyMap clinicalDataMap,
      String entityId,
      String studyId,
      boolean negateFilters) {
    int count = 0;

    for (ClinicalDataFilter filter : attributes) {
      if (clinicalDataMap.containsKey(studyId, entityId, filter.getAttributeId())) {
        String attrValue = (String) clinicalDataMap.get(studyId, entityId, filter.getAttributeId());
        Range<BigDecimal> rangeValue = calculateRangeValueForAttr(attrValue);

        // find range filters
        List<Range<BigDecimal>> ranges =
            filter.getValues().stream()
                .map(this::calculateRangeValueForFilter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // find special value filters
        List<String> specialValues =
            filter.getValues().stream()
                .filter(f -> f.getValue() != null)
                .map(f -> f.getValue().toUpperCase())
                .collect(Collectors.toList());

        if (rangeValue != null) {
          if (negateFilters ^ ranges.stream().anyMatch(r -> r.encloses(rangeValue))) {
            count++;
          }
        } else if (negateFilters ^ specialValues.contains(attrValue.toUpperCase())) {
          count++;
        }
      } else if (negateFilters ^ containsNA(filter)) {
        count++;
      }
    }

    return count;
  }

  private Range<BigDecimal> calculateRangeValueForAttr(String attrValue) {
    if (attrValue == null) {
      return null;
    }

    BigDecimal min = null;
    BigDecimal max = null;

    String value = attrValue.trim();

    String lte = "<=";
    String lt = "<";
    String gte = ">=";
    String gt = ">";

    boolean startInclusive = true;
    boolean endInclusive = true;

    try {
      if (value.startsWith(lte)) {
        max = new BigDecimal(value.substring(lte.length()));
      } else if (value.startsWith(lt)) {
        max = new BigDecimal(value.substring(lt.length()));
        endInclusive = false;
      } else if (value.startsWith(gte)) {
        min = new BigDecimal(value.substring(gte.length()));
      } else if (value.startsWith(gt)) {
        min = new BigDecimal(value.substring(gt.length()));
        startInclusive = false;
      } else {
        min = max = new BigDecimal(attrValue);
      }
    } catch (Exception e) {
      // invalid range -- TODO: also support ranges like 20-30?
      return null;
    }

    return DataBinHelper.calcRange(min, startInclusive, max, endInclusive);
  }

  private Range<BigDecimal> calculateRangeValueForFilter(DataFilterValue filterValue) {
    BigDecimal start = filterValue.getStart();
    BigDecimal end = filterValue.getEnd();

    // default: (start, end]
    boolean startInclusive = false;
    boolean endInclusive = true;

    // special case: end == start (both inclusive)
    if (end != null && end.equals(start)) {
      startInclusive = true;
    }

    return DataBinHelper.calcRange(start, startInclusive, end, endInclusive);
  }

  private Boolean containsNA(ClinicalDataFilter filter) {
    return filter.getValues().stream()
        .anyMatch(r -> r.getValue() != null && r.getValue().toUpperCase().equals("NA"));
  }

  public static MultiKeyMap<String, String> buildClinicalDataMap(List<ClinicalData> clinicalDatas) {
    MultiKeyMap<String, String> clinicalDataMap = new MultiKeyMap<>();

    clinicalDatas.forEach(
        clinicalData ->
            clinicalDataMap.put(
                clinicalData.getStudyId(),
                clinicalData.getSampleId(),
                clinicalData.getAttrId(),
                clinicalData.getAttrValue()));

    return clinicalDataMap;
  }
}
