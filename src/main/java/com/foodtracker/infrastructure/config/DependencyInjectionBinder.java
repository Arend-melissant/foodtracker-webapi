package com.foodtracker.infrastructure.config;

import com.foodtracker.application.port.inbound.*;
import com.foodtracker.application.port.outbound.*;
import com.foodtracker.application.service.AdverseReactionService;
import com.foodtracker.application.service.AuthService;
import com.foodtracker.application.service.CorrelationAnalysisService;
import com.foodtracker.application.service.FoodTrackingService;
import com.foodtracker.infrastructure.adapter.outbound.auth.GoogleOAuthTokenVerifier;
import com.foodtracker.infrastructure.adapter.outbound.eventbus.InMemoryEventPublisher;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteAdverseReactionRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteFoodEntryRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteUserRepositoryAdapter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyInjectionBinder extends AbstractBinder {

    @Override
    protected void configure() {
        // Outbound Ports & Adapters (Singletons)
        UserRepositoryPort userRepository = new SqliteUserRepositoryAdapter();
        FoodEntryRepositoryPort foodRepository = new SqliteFoodEntryRepositoryAdapter();
        AdverseReactionRepositoryPort reactionRepository = new SqliteAdverseReactionRepositoryAdapter();
        InMemoryEventPublisher eventPublisher = new InMemoryEventPublisher();
        TokenVerificationPort tokenVerifier = new GoogleOAuthTokenVerifier();

        bind(userRepository).to(UserRepositoryPort.class);
        bind(foodRepository).to(FoodEntryRepositoryPort.class);
        bind(reactionRepository).to(AdverseReactionRepositoryPort.class);
        bind(eventPublisher).to(EventPublisherPort.class);
        bind(tokenVerifier).to(TokenVerificationPort.class);

        // Application Services & Inbound Use Cases (Singletons)
        AuthService authService = new AuthService(userRepository, tokenVerifier, eventPublisher);
        FoodTrackingService foodService = new FoodTrackingService(foodRepository, eventPublisher);
        AdverseReactionService reactionService = new AdverseReactionService(reactionRepository, eventPublisher);
        CorrelationAnalysisService correlationService = new CorrelationAnalysisService(foodRepository, reactionRepository);

        bind(authService).to(AuthenticateUserUseCase.class);
        bind(foodService).to(LogFoodUseCase.class);
        bind(foodService).to(GetFoodEntriesUseCase.class);
        bind(reactionService).to(LogAdverseReactionUseCase.class);
        bind(reactionService).to(GetAdverseReactionsUseCase.class);
        bind(correlationService).to(AnalyzeCorrelationsUseCase.class);
    }
}

