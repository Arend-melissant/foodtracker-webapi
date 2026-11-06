package com.foodtracker.infrastructure.adapter.inbound.rest;

import com.foodtracker.application.port.inbound.AnalyzeCorrelationsUseCase;
import com.foodtracker.application.port.inbound.GetAdverseReactionsUseCase;
import com.foodtracker.application.port.inbound.LogAdverseReactionUseCase;
import com.foodtracker.domain.exception.EntityNotFoundException;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodReactionCorrelation;
import com.foodtracker.domain.model.Severity;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.AdverseReactionDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.CorrelationReportDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.CreateAdverseReactionDto;
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

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Path("/api/v1/reactions")
@Authenticated
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Adverse Reactions", description = "Track symptoms and adverse effects occurring after food consumption")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AdverseReactionController {

    private final LogAdverseReactionUseCase logReactionUseCase;
    private final GetAdverseReactionsUseCase getReactionsUseCase;
    private final AnalyzeCorrelationsUseCase analyzeCorrelationsUseCase;

    @Inject
    public AdverseReactionController(
            LogAdverseReactionUseCase logReactionUseCase,
            GetAdverseReactionsUseCase getReactionsUseCase,
            AnalyzeCorrelationsUseCase analyzeCorrelationsUseCase) {
        this.logReactionUseCase = logReactionUseCase;
        this.getReactionsUseCase = getReactionsUseCase;
        this.analyzeCorrelationsUseCase = analyzeCorrelationsUseCase;
    }

    @GET
    @Operation(summary = "List adverse reactions", description = "Returns all adverse reactions logged by the user.")
    @ApiResponse(responseCode = "200", description = "List of adverse reactions")
    public Response getReactions(@Context SecurityContext securityContext) {
        String userId = securityContext.getUserPrincipal().getName();
        List<AdverseReaction> reactions = getReactionsUseCase.getReactionsForUser(userId);
        List<AdverseReactionDto> dtoList = reactions.stream()
                .map(AdverseReactionDto::fromDomain)
                .toList();

        return Response.ok(dtoList).build();
    }

    @POST
    @Operation(summary = "Log an adverse reaction", description = "Records an adverse reaction/symptom (e.g. headache, hives, stomach cramp).")
    @ApiResponse(responseCode = "201", description = "Reaction logged successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public Response logReaction(@Context SecurityContext securityContext, CreateAdverseReactionDto request) {
        String userId = securityContext.getUserPrincipal().getName();

        Severity severity = Severity.MODERATE;
        if (request.getSeverity() != null && !request.getSeverity().isBlank()) {
            try {
                severity = Severity.valueOf(request.getSeverity().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Instant occurredAt = Instant.now();
        if (request.getOccurredAt() != null && !request.getOccurredAt().isBlank()) {
            try {
                occurredAt = Instant.parse(request.getOccurredAt());
            } catch (Exception ignored) {}
        }

        LogAdverseReactionUseCase.Command command = new LogAdverseReactionUseCase.Command(
                userId,
                request.getSymptom(),
                severity,
                occurredAt,
                request.getNotes(),
                request.getSuspectedFoodEntryId()
        );

        AdverseReaction reaction = logReactionUseCase.logReaction(command);
        AdverseReactionDto dto = AdverseReactionDto.fromDomain(reaction);

        return Response.created(URI.create("/api/v1/reactions/" + reaction.getId()))
                .entity(dto)
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get adverse reaction details", description = "Fetches a specific reaction by ID.")
    @ApiResponse(responseCode = "200", description = "Reaction found")
    @ApiResponse(responseCode = "404", description = "Reaction not found")
    public Response getReactionById(@Context SecurityContext securityContext, @PathParam("id") String id) {
        String userId = securityContext.getUserPrincipal().getName();
        AdverseReaction reaction = getReactionsUseCase.getReactionById(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found with id: " + id));

        return Response.ok(AdverseReactionDto.fromDomain(reaction)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete adverse reaction", description = "Deletes a reaction entry.")
    @ApiResponse(responseCode = "204", description = "Reaction deleted successfully")
    public Response deleteReaction(@Context SecurityContext securityContext, @PathParam("id") String id) {
        String userId = securityContext.getUserPrincipal().getName();
        boolean deleted = logReactionUseCase.deleteReaction(id, userId);
        if (!deleted) {
            throw new EntityNotFoundException("Reaction not found or cannot be deleted: " + id);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/suspect-foods")
    @Operation(summary = "Find preceding foods for an adverse reaction",
               description = "Correlates an adverse reaction with all foods consumed in preceding hours (default: 72 hours / 3 days).")
    @ApiResponse(responseCode = "200", description = "Correlation report generated")
    @ApiResponse(responseCode = "404", description = "Reaction not found")
    public Response getSuspectFoods(
            @Context SecurityContext securityContext,
            @PathParam("id") String id,
            @Parameter(description = "Hours to look back before the reaction (default: 72)")
            @QueryParam("lookbackHours") @DefaultValue("72") int lookbackHours
    ) {
        String userId = securityContext.getUserPrincipal().getName();
        FoodReactionCorrelation correlation = analyzeCorrelationsUseCase.getSuspectFoodsForReaction(id, userId, lookbackHours);
        return Response.ok(CorrelationReportDto.fromDomain(correlation)).build();
    }
}

