package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.ExpressionEnrichment;
import org.cbioportal.legacy.model.GenericAssayEnrichment;
import org.cbioportal.legacy.model.GenomicEnrichment;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.service.ExpressionEnrichmentService;
import org.cbioportal.legacy.service.exception.GenericAssayNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Enrichments", description = " ")
public class ExpressionEnrichmentController {
  @Autowired private ExpressionEnrichmentService expressionEnrichmentService;

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/expression-enrichments/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch genomic enrichments in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = GenomicEnrichment.class))))
  public ResponseEntity<List<GenomicEnrichment>> fetchGenomicEnrichments(
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(description = "Type of the enrichment e.g. SAMPLE or PATIENT")
          @RequestParam(defaultValue = "SAMPLE")
          EnrichmentType enrichmentType,
      @Parameter(
              required = true,
              description = "List of groups containing sample and molecular profile identifiers")
          @Valid
          @RequestBody(required = false)
          List<MolecularProfileCasesGroupFilter> groups,
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(
              required = false,
              value = "interceptedMolecularProfileCasesGroupFilters")
          List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters)
      throws MolecularProfileNotFoundException {

    return new ResponseEntity<>(
        fetchExpressionEnrichments(
            enrichmentType, interceptedMolecularProfileCasesGroupFilters, false),
        HttpStatus.OK);
  }

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/generic-assay-enrichments/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch generic assay enrichments in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = GenericAssayEnrichment.class))))
  public ResponseEntity<List<GenericAssayEnrichment>> fetchGenericAssayEnrichments(
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(description = "Type of the enrichment e.g. SAMPLE or PATIENT")
          @RequestParam(defaultValue = "SAMPLE")
          EnrichmentType enrichmentType,
      @Parameter(
              required = true,
              description = "List of groups containing sample and molecular profile identifiers")
          @Valid
          @RequestBody(required = false)
          List<MolecularProfileCasesGroupFilter> groups,
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(
              required = false,
              value = "interceptedMolecularProfileCasesGroupFilters")
          List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters)
      throws MolecularProfileNotFoundException,
          UnsupportedOperationException,
          GenericAssayNotFoundException {

    return new ResponseEntity<>(
        fetchExpressionEnrichments(
            enrichmentType, interceptedMolecularProfileCasesGroupFilters, true),
        HttpStatus.OK);
  }

  private <S extends ExpressionEnrichment> List<S> fetchExpressionEnrichments(
      EnrichmentType enrichmentType,
      List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
      Boolean isRequestForGenericAssayEnrichments)
      throws MolecularProfileNotFoundException {

    Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet =
        interceptedMolecularProfileCasesGroupFilters.stream()
            .collect(
                Collectors.toMap(
                    MolecularProfileCasesGroupFilter::getName,
                    MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

    Set<String> molecularProfileIds =
        groupCaseIdentifierSet.values().stream()
            .flatMap(
                molecularProfileCaseSet ->
                    molecularProfileCaseSet.stream()
                        .map(MolecularProfileCaseIdentifier::getMolecularProfileId))
            .collect(Collectors.toSet());

    if (molecularProfileIds.size() > 1) {
      throw new UnsupportedOperationException(
          "Multi-study expression enrichments is not yet implemented");
    }

    if (isRequestForGenericAssayEnrichments) {
      return (List<S>)
          expressionEnrichmentService.getGenericAssayNumericalEnrichments(
              molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType);
    }

    return (List<S>)
        expressionEnrichmentService.getGenomicEnrichments(
            molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType);
  }
}
