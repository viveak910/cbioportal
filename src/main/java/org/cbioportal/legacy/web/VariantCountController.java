package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.VariantCount;
import org.cbioportal.legacy.service.VariantCountService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.VariantCountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Variant Counts", description = " ")
public class VariantCountController {

  private static final int VARIANT_COUNT_MAX_PAGE_SIZE = 50000;

  @Autowired private VariantCountService variantCountService;

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/variant-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get counts of specific variants within a mutation molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = VariantCount.class))))
  public ResponseEntity<List<VariantCount>> fetchVariantCounts(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
          @PathVariable
          String molecularProfileId,
      @Parameter(required = true, description = "List of variant count identifiers")
          @Size(min = 1, max = VARIANT_COUNT_MAX_PAGE_SIZE)
          @RequestBody
          List<VariantCountIdentifier> variantCountIdentifiers)
      throws MolecularProfileNotFoundException {

    List<Integer> entrezGeneIds = new ArrayList<>();
    List<String> keywords = new ArrayList<>();

    for (VariantCountIdentifier variantCountIdentifier : variantCountIdentifiers) {

      entrezGeneIds.add(variantCountIdentifier.getEntrezGeneId());
      keywords.add(variantCountIdentifier.getKeyword());
    }

    return new ResponseEntity<>(
        variantCountService.fetchVariantCounts(molecularProfileId, entrezGeneIds, keywords),
        HttpStatus.OK);
  }
}
