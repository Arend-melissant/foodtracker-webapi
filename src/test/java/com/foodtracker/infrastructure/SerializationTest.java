package com.foodtracker.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.CreateFoodEntryDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.FoodEntryDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class SerializationTest {

    @Test
    @DisplayName("Should serialize and deserialize DTO to/from JSON")
    void testJsonSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateFoodEntryDto dto = new CreateFoodEntryDto(
                "Salmon Sushi",
                "DINNER",
                "salmon, rice, nori",
                "2026-11-06T19:00:00Z",
                "Fresh wasabi included"
        );

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("\"foodName\":\"Salmon Sushi\"");

        CreateFoodEntryDto deserialized = objectMapper.readValue(json, CreateFoodEntryDto.class);
        assertThat(deserialized.getFoodName()).isEqualTo("Salmon Sushi");
        assertThat(deserialized.getMealType()).isEqualTo("DINNER");
    }

    @Test
    @DisplayName("Should serialize and deserialize DTO to/from XML via JAXB")
    void testXmlSerializationWithJaxb() throws Exception {
        JAXBContext context = JAXBContext.newInstance(FoodEntryDto.class);
        FoodEntryDto dto = new FoodEntryDto(
                "entry-100",
                "user-999",
                "Almond Croissant",
                "BREAKFAST",
                "almonds, butter, flour, sugar",
                "2026-11-06T08:30:00Z",
                "Bakery treat"
        );

        // Marshall to XML
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(dto, writer);
        String xml = writer.toString();

        assertThat(xml).contains("<foodName>Almond Croissant</foodName>");
        assertThat(xml).contains("<mealType>BREAKFAST</mealType>");

        // Unmarshall from XML
        Unmarshaller unmarshaller = context.createUnmarshaller();
        FoodEntryDto deserialized = (FoodEntryDto) unmarshaller.unmarshal(new StringReader(xml));

        assertThat(deserialized.getId()).isEqualTo("entry-100");
        assertThat(deserialized.getFoodName()).isEqualTo("Almond Croissant");
        assertThat(deserialized.getIngredients()).contains("almonds");
    }

    @Test
    @DisplayName("Should serialize and deserialize DTO to/from XML via Jackson XmlMapper")
    void testXmlSerializationWithJacksonXml() throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        CreateFoodEntryDto dto = new CreateFoodEntryDto(
                "Greek Salad",
                "LUNCH",
                "feta, cucumber, olives, olive oil",
                "2026-11-06T12:30:00Z",
                "Low carb"
        );

        String xml = xmlMapper.writeValueAsString(dto);
        assertThat(xml).contains("<foodName>Greek Salad</foodName>");

        CreateFoodEntryDto deserialized = xmlMapper.readValue(xml, CreateFoodEntryDto.class);
        assertThat(deserialized.getFoodName()).isEqualTo("Greek Salad");
    }
}

