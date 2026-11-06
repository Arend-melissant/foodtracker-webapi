package com.foodtracker.infrastructure.adapter.inbound.rest;

import com.foodtracker.application.port.inbound.AnalyzeCorrelationsUseCase;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.AggregatedInsightDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/v1/analytics")
@Authenticated
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Analytics & Correlation", description = "Pinpoint food triggers and adverse reaction patterns")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CorrelationController {

    private final AnalyzeCorrelationsUseCase analyzeCorrelationsUseCase;

    @Inject
    public CorrelationController(AnalyzeCorrelationsUseCase analyzeCorrelationsUseCase) {
        this.analyzeCorrelationsUseCase = analyzeCorrelationsUseCase;
    }

    @GET
    @Path("/correlations")
    @Operation(summary = "Get aggregated food trigger insights",
               description = "Analyzes frequency of foods consumed 1-3 days prior to adverse symptoms to identify recurring patterns.")
    @ApiResponse(responseCode = "200", description = "Analysis list of suspect foods")
    public Response getTopSuspectFoods(
            @Context SecurityContext securityContext,
            @Parameter(description = "Hours to look back prior to each reaction (default: 72)")
            @QueryParam("lookbackHours") @DefaultValue("72") int lookbackHours
    ) {
        String userId = securityContext.getUserPrincipal().getName();
        List<AnalyzeCorrelationsUseCase.AggregatedInsight> insights =
                analyzeCorrelationsUseCase.getTopSuspectFoods(userId, lookbackHours);

        List<AggregatedInsightDto> dtoList = insights.stream()
                .map(AggregatedInsightDto::fromDomain)
                .toList();

        return Response.ok(dtoList).build();
    }
}

