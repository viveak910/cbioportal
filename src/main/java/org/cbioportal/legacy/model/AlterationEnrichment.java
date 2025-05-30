package org.cbioportal.legacy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class AlterationEnrichment implements Serializable {

  @NotNull private Integer entrezGeneId;
  @NotNull private String hugoGeneSymbol;
  private String cytoband;
  private BigDecimal pValue;
  @NotNull private List<CountSummary> counts;

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(Integer entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public String getHugoGeneSymbol() {
    return hugoGeneSymbol;
  }

  public void setHugoGeneSymbol(String hugoGeneSymbol) {
    this.hugoGeneSymbol = hugoGeneSymbol;
  }

  public String getCytoband() {
    return cytoband;
  }

  public void setCytoband(String cytoband) {
    this.cytoband = cytoband;
  }

  @JsonProperty("pValue")
  public BigDecimal getpValue() {
    return pValue;
  }

  public void setpValue(BigDecimal pValue) {
    this.pValue = pValue;
  }

  public List<CountSummary> getCounts() {
    return counts;
  }

  public void setCounts(List<CountSummary> counts) {
    this.counts = counts;
  }
}
