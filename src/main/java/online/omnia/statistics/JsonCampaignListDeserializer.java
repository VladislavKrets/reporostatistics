package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 27.12.2017.
 */
public class JsonCampaignListDeserializer implements JsonDeserializer<List<Integer>>{

    @Override
    public List<Integer> deserialize(JsonElement jsonElement, Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray array = object.get("campaigns").getAsJsonArray();
        List<Integer> campaigns = new ArrayList<>();
        for (JsonElement element : array) {
            campaigns.add(element.getAsJsonObject().get("campaign").getAsInt());
        }
        return campaigns;
    }
}
