package com.foodtracker.infrastructure.adapter.inbound.rest;

import com.foodtracker.application.port.inbound.GetFoodEntriesUseCase;
import com.foodtracker.application.port.inbound.LogFoodUseCase;
import com.foodtracker.domain.exception.EntityNotFoundException;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.CreateFoodEntryDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.FoodEntryDto;
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

@Path("/api/v1/foods")
@Authenticated
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Food Tracking", description = "Track meals, foods, and ingredients consumed")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class FoodEntryController {

    private final LogFoodUseCase logFoodUseCase;
    private final GetFoodEntriesUseCase getFoodEntriesUseCase;

    @Inject
    public FoodEntryController(LogFoodUseCase logFoodUseCase, GetFoodEntriesUseCase getFoodEntriesUseCase) {
        this.logFoodUseCase = logFoodUseCase;
        this.getFoodEntriesUseCase = getFoodEntriesUseCase;
    }

    @GET
    @Operation(summary = "List food entries", description = "Returns food entries for the authenticated user, optionally filtered by date range.")
    @ApiResponse(responseCode = "200", description = "List of food entries")
    public Response getFoodEntries(
            @Context SecurityContext securityContext,
            @Parameter(description = "ISO-8601 start time (e.g. 2026-11-01T00:00:00Z)") @QueryParam("start") String startStr,
            @Parameter(description = "ISO-8601 end time (e.g. 2026-11-06T23:59:59Z)") @QueryParam("end") String endStr
    ) {
        String userId = securityContext.getUserPrincipal().getName();
        List<FoodEntry> entries;

        if (startStr != null && endStr != null) {
            Instant start = Instant.parse(startStr);
            Instant end = Instant.parse(endStr);
            entries = getFoodEntriesUseCase.getFoodEntriesForUserInRange(userId, start, end);
        } else {
            entries = getFoodEntriesUseCase.getFoodEntriesForUser(userId);
        }

        List<FoodEntryDto> dtoList = entries.stream()
                .map(FoodEntryDto::fromDomain)
                .toList();

        return Response.ok(dtoList).build();
    }

    @POST
    @Operation(summary = "Log food consumption", description = "Records a food or meal item consumed by the user.")
    @ApiResponse(responseCode = "201", description = "Food entry successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public Response logFood(@Context SecurityContext securityContext, CreateFoodEntryDto request) {
        String userId = securityContext.getUserPrincipal().getName();

        MealType mealType = MealType.OTHER;
        if (request.getMealType() != null && !request.getMealType().isBlank()) {
            try {
                mealType = MealType.valueOf(request.getMealType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Instant consumedAt = Instant.now();
        if (request.getConsumedAt() != null && !request.getConsumedAt().isBlank()) {
            try {
                consumedAt = Instant.parse(request.getConsumedAt());
            } catch (Exception ignored) {}
        }

        LogFoodUseCase.Command command = new LogFoodUseCase.Command(
                userId,
                request.getFoodName(),
                mealType,
                request.getIngredients(),
                consumedAt,
                request.getNotes()
        );

        FoodEntry entry = logFoodUseCase.logFood(command);
        FoodEntryDto dto = FoodEntryDto.fromDomain(entry);

        return Response.created(URI.create("/api/v1/foods/" + entry.getId()))
                .entity(dto)
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get food entry details", description = "Fetches a specific food entry by ID.")
    @ApiResponse(responseCode = "200", description = "Food entry found")
    @ApiResponse(responseCode = "404", description = "Food entry not found")
    public Response getFoodEntryById(@Context SecurityContext securityContext, @PathParam("id") String id) {
        String userId = securityContext.getUserPrincipal().getName();
        FoodEntry entry = getFoodEntriesUseCase.getFoodEntryById(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Food entry not found with id: " + id));

        return Response.ok(FoodEntryDto.fromDomain(entry)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete food entry", description = "Deletes a food entry.")
    @ApiResponse(responseCode = "204", description = "Food entry deleted successfully")
    @ApiResponse(responseCode = "404", description = "Food entry not found")
    public Response deleteFoodEntry(@Context SecurityContext securityContext, @PathParam("id") String id) {
        String userId = securityContext.getUserPrincipal().getName();
        boolean deleted = logFoodUseCase.deleteFood(id, userId);
        if (!deleted) {
            throw new EntityNotFoundException("Food entry not found or cannot be deleted: " + id);
        }
        return Response.noContent().build();
    }
}

