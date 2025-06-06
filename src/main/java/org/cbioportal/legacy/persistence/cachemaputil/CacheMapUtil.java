package org.cbioportal.legacy.persistence.cachemaputil;

import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;

public interface CacheMapUtil {
  Map<String, MolecularProfile> getMolecularProfileMap();

  Map<String, SampleList> getSampleListMap();

  Map<String, CancerStudy> getCancerStudyMap();

  boolean hasCacheEnabled();
}
