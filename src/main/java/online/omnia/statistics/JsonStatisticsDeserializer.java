package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 27.12.2017.
 */
public class JsonStatisticsDeserializer implements JsonDeserializer<List<SourceStatisticsEntity>>{
    @Override
    public List<SourceStatisticsEntity> deserialize(JsonElement jsonElement, Type type,
                                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<SourceStatisticsEntity> sourceStatisticsEntities = new ArrayList<>();
        if (!jsonElement.isJsonArray()) return sourceStatisticsEntities;
        JsonArray array = jsonElement.getAsJsonArray();
        SourceStatisticsEntity sourceStatisticsEntity;
        for (JsonElement element : array) {
            sourceStatisticsEntity = new SourceStatisticsEntity();
            sourceStatisticsEntity.setImpressions(element.getAsJsonObject().get("impressions").getAsInt());
            sourceStatisticsEntity.setClicks(element.getAsJsonObject().get("clicks").getAsInt());
            sourceStatisticsEntity.setConversions(element.getAsJsonObject().get("conversions").getAsInt());
            sourceStatisticsEntity.setSpent(element.getAsJsonObject().get("spend").getAsDouble());
            sourceStatisticsEntities.add(sourceStatisticsEntity);
        }
        return sourceStatisticsEntities;
    }
}
