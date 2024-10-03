package com.luminary.apieden.mapper;

import com.luminary.apieden.model.database.Card;
import com.luminary.apieden.model.request.CardRequest;
import com.luminary.apieden.model.response.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "user", ignore = true)
    Card toCards(CardRequest request);

    @Mapping(source = "user.id", target = "userId")
    CardResponse toCardResponse(Card card);
}